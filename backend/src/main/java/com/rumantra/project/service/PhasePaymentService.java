package com.rumantra.project.service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.Client;
import com.rumantra.client.domain.Project;
import com.rumantra.client.domain.ProjectStatus;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.integration.xendit.XenditService;
import com.rumantra.integration.xendit.dto.XenditInvoiceRequest;
import com.rumantra.integration.xendit.dto.XenditInvoiceResponse;
import com.rumantra.integration.xendit.dto.XenditInvoiceWebhook;
import com.rumantra.integration.xendit.dto.XenditPayoutCallback;
import com.rumantra.integration.xendit.dto.XenditPayoutRequest;
import com.rumantra.integration.xendit.dto.XenditPayoutResponse;
import com.rumantra.payment.domain.PhasePayment;
import com.rumantra.payment.domain.PhasePaymentStatus;
import com.rumantra.payment.repository.PhasePaymentRepository;
import com.rumantra.project.domain.DisbursementStatus;
import com.rumantra.project.domain.PhaseActorType;
import com.rumantra.project.domain.PhaseDeliverable;
import com.rumantra.project.domain.PhaseDisbursement;
import com.rumantra.project.domain.PhaseProcessLog;
import com.rumantra.project.domain.PhaseStatus;
import com.rumantra.project.domain.ProjectPhase;
import com.rumantra.project.dto.DeliverableResponse;
import com.rumantra.project.dto.DeliverableUploadRequest;
import com.rumantra.project.dto.DisbursementRequest;
import com.rumantra.project.dto.DisbursementResponse;
import com.rumantra.project.dto.PhaseCreateRequest;
import com.rumantra.project.dto.PhaseLogResponse;
import com.rumantra.project.dto.PhaseResponse;
import com.rumantra.project.repository.PhaseDeliverableRepository;
import com.rumantra.project.repository.PhaseDisbursementRepository;
import com.rumantra.project.repository.PhaseProcessLogRepository;
import com.rumantra.project.repository.ProjectPhaseRepository;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PhasePaymentService {

  @Autowired private ProjectPhaseRepository projectPhaseRepository;
  @Autowired private PhaseDisbursementRepository phaseDisbursementRepository;
  @Autowired private PhaseDeliverableRepository phaseDeliverableRepository;
  @Autowired private PhaseProcessLogRepository phaseProcessLogRepository;
  @Autowired private PhasePaymentRepository phasePaymentRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private ClientRepository clientRepository;
  @Autowired private ArchitectRepository architectRepository;
  @Autowired private BidRepository bidRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private XenditService xenditService;
  @Autowired private com.rumantra.shared.storage.FileStorageService fileStorageService;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  @Transactional
  public PhaseResponse initializePhase(Long projectId, Long clientUserId, PhaseCreateRequest req) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_NOT_FOUND));

    if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
      throw new BusinessException(ExceptionConstants.PROJECT_NOT_IN_PROGRESS);
    }

    Client client =
        clientRepository
            .findByUserId(clientUserId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS));

    if (!project.getClient().getId().equals(client.getId())) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS);
    }

    List<ProjectPhase> existing =
        projectPhaseRepository.findByProjectIdOrderByPhaseNumberAsc(projectId);
    int nextPhaseNumber =
        existing.stream().mapToInt(ProjectPhase::getPhaseNumber).max().orElse(0) + 1;

    ProjectPhase phase =
        ProjectPhase.builder()
            .project(project)
            .phaseNumber(nextPhaseNumber)
            .title(req.getTitle())
            .description(req.getDescription())
            .amount(req.getAmount())
            .dueDate(req.getDueDate())
            .build();
    phase = projectPhaseRepository.save(phase);

    log(phase, null, PhaseActorType.SYSTEM, "PHASE_CREATED", null, PhaseStatus.PENDING, null);

    return toPhaseResponse(phase, null, List.of());
  }

  @Transactional
  public PhasePayment createInvoiceForPhase(Long phaseId, Long clientUserId) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyClientOwnsProject(clientUserId, phase.getProject().getId());

    if (phase.getStatus() != PhaseStatus.PENDING) {
      throw new BusinessException(ExceptionConstants.PHASE_WRONG_STATUS);
    }

    Project project = phase.getProject();
    Client client = project.getClient();

    String externalId = String.format("proj_phase_%d_%d", phaseId, System.currentTimeMillis());

    XenditInvoiceRequest.Customer customer =
        XenditInvoiceRequest.Customer.builder()
            .givenNames(client.getUser().getFirstName())
            .surname(client.getUser().getLastName())
            .email(client.getUser().getEmail())
            .build();

    XenditInvoiceRequest.InvoiceItem item =
        XenditInvoiceRequest.InvoiceItem.builder()
            .name(String.format("Phase %d: %s", phase.getPhaseNumber(), phase.getTitle()))
            .quantity(1)
            .price(phase.getAmount())
            .category("Architecture Service")
            .build();

    XenditInvoiceRequest.CustomerNotificationPreference notifPref =
        XenditInvoiceRequest.CustomerNotificationPreference.builder()
            .invoiceCreated(new ArrayList<>())
            .invoiceReminder(new ArrayList<>())
            .invoicePaid(Arrays.asList("email"))
            .build();

    XenditInvoiceRequest xenditReq =
        XenditInvoiceRequest.builder()
            .externalId(externalId)
            .amount(phase.getAmount())
            .description(
                String.format(
                    "Phase %d: %s — Project: %s",
                    phase.getPhaseNumber(), phase.getTitle(), project.getTitle()))
            .currency("IDR")
            .invoiceDuration(86400)
            .successRedirectUrl(frontendUrl + "/client/projects/" + project.getId() + "/phases")
            .failureRedirectUrl(frontendUrl + "/client/projects/" + project.getId() + "/phases")
            .customer(customer)
            .customerNotificationPreference(notifPref)
            .items(Arrays.asList(item))
            .build();

    XenditInvoiceResponse xenditResp = xenditService.createInvoice(xenditReq);

    LocalDateTime expiresAt =
        ZonedDateTime.parse(xenditResp.getExpiryDate(), DateTimeFormatter.ISO_DATE_TIME)
            .toLocalDateTime();

    PhasePayment payment =
        phasePaymentRepository
            .findByProjectPhaseId(phaseId)
            .map(
                existing -> {
                  existing.setXenditInvoiceId(xenditResp.getId());
                  existing.setXenditReferenceId(externalId);
                  existing.setPaymentLink(xenditResp.getInvoiceUrl());
                  existing.setExpiresAt(expiresAt);
                  existing.setStatus(PhasePaymentStatus.PENDING);
                  return phasePaymentRepository.save(existing);
                })
            .orElseGet(
                () -> {
                  List<Bid> acceptedBids =
                      bidRepository.findByProjectIdAndStatus(project.getId(), BidStatus.ACCEPTED);
                  Architect architect =
                      acceptedBids.isEmpty() ? null : acceptedBids.get(0).getArchitect();

                  return phasePaymentRepository.save(
                      PhasePayment.builder()
                          .projectPhase(phase)
                          .project(project)
                          .client(client)
                          .architect(architect)
                          .amount(phase.getAmount())
                          .status(PhasePaymentStatus.PENDING)
                          .xenditInvoiceId(xenditResp.getId())
                          .xenditReferenceId(externalId)
                          .paymentLink(xenditResp.getInvoiceUrl())
                          .expiresAt(expiresAt)
                          .build());
                });

    phase.setStatus(PhaseStatus.BILLED);
    projectPhaseRepository.save(phase);

    log(
        phase,
        clientUserId,
        PhaseActorType.CLIENT,
        "PHASE_BILLED",
        PhaseStatus.PENDING,
        PhaseStatus.BILLED,
        null);

    return payment;
  }

  @Transactional
  public void handlePaymentWebhook(XenditInvoiceWebhook webhook) {
    String externalId = webhook.getExternalId();

    PhasePayment payment =
        phasePaymentRepository
            .findByXenditReferenceId(externalId)
            .filter(p -> p.getProjectPhase() != null)
            .orElse(null);

    if (payment == null) {
      log.info("No project-phase payment found for external_id: {}", externalId);
      return;
    }

    if (payment.getStatus() == PhasePaymentStatus.COMPLETED) {
      log.info("Project-phase payment already completed (idempotent): {}", payment.getId());
      return;
    }

    payment.setStatus(PhasePaymentStatus.COMPLETED);
    payment.setXenditInvoiceId(webhook.getId());
    payment.setPaymentMethod(webhook.getPaymentMethod());
    payment.setPaymentChannel(webhook.getPaymentChannel());
    payment.setCompletedAt(LocalDateTime.now());
    phasePaymentRepository.save(payment);

    ProjectPhase phase = payment.getProjectPhase();
    PhaseStatus prev = phase.getStatus();
    phase.setStatus(PhaseStatus.IN_PROGRESS);
    projectPhaseRepository.save(phase);

    log(
        phase,
        null,
        PhaseActorType.XENDIT,
        "PAYMENT_RECEIVED",
        prev,
        PhaseStatus.IN_PROGRESS,
        null);

    log.info("Project phase {} moved to IN_PROGRESS after payment", phase.getId());
  }

  @Transactional
  public DeliverableResponse addDeliverable(
      Long phaseId, Long architectUserId, DeliverableUploadRequest req) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyArchitectOwnsProject(architectUserId, phase.getProject().getId());

    if (phase.getStatus() != PhaseStatus.IN_PROGRESS) {
      throw new BusinessException(ExceptionConstants.PHASE_WRONG_STATUS);
    }

    User user =
        userRepository
            .findById(architectUserId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS));

    PhaseDeliverable deliverable =
        PhaseDeliverable.builder()
            .phase(phase)
            .uploadedBy(user)
            .filePath(req.getFilePath())
            .fileType(req.getFileType())
            .description(req.getDescription())
            .build();
    deliverable = phaseDeliverableRepository.save(deliverable);

    log(phase, architectUserId, PhaseActorType.ARCHITECT, "DELIVERABLE_UPLOADED", null, null, null);

    return toDeliverableResponse(deliverable);
  }

  @Transactional
  public DeliverableResponse uploadDeliverableFile(
      Long phaseId,
      Long architectUserId,
      org.springframework.web.multipart.MultipartFile file,
      String description) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyArchitectOwnsProject(architectUserId, phase.getProject().getId());

    if (phase.getStatus() != PhaseStatus.IN_PROGRESS) {
      throw new BusinessException(ExceptionConstants.PHASE_WRONG_STATUS);
    }

    User user =
        userRepository
            .findById(architectUserId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS));

    String storedPath = fileStorageService.uploadFile(file, "deliverables/" + phaseId);
    String fileType = file.getContentType();

    PhaseDeliverable deliverable =
        PhaseDeliverable.builder()
            .phase(phase)
            .uploadedBy(user)
            .filePath(storedPath)
            .fileType(fileType)
            .description(description)
            .build();
    deliverable = phaseDeliverableRepository.save(deliverable);

    log(phase, architectUserId, PhaseActorType.ARCHITECT, "DELIVERABLE_UPLOADED", null, null, null);

    return toDeliverableResponse(deliverable);
  }

  @Transactional
  public PhaseResponse submitForReview(Long phaseId, Long architectUserId) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyArchitectOwnsProject(architectUserId, phase.getProject().getId());

    if (phase.getStatus() != PhaseStatus.IN_PROGRESS) {
      throw new BusinessException(ExceptionConstants.PHASE_WRONG_STATUS);
    }

    List<PhaseDeliverable> deliverables =
        phaseDeliverableRepository.findByPhaseIdOrderByUploadedAtAsc(phaseId);
    if (deliverables.isEmpty()) {
      throw new BusinessException(ExceptionConstants.NO_DELIVERABLES_YET);
    }

    phase.setStatus(PhaseStatus.DELIVERED);
    projectPhaseRepository.save(phase);

    log(
        phase,
        architectUserId,
        PhaseActorType.ARCHITECT,
        "PHASE_SUBMITTED_FOR_REVIEW",
        PhaseStatus.IN_PROGRESS,
        PhaseStatus.DELIVERED,
        null);

    PhasePayment payment = phasePaymentRepository.findByProjectPhaseId(phaseId).orElse(null);
    return toPhaseResponse(phase, payment, deliverables);
  }

  @Transactional
  public PhaseResponse requestRevision(Long phaseId, Long clientUserId) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyClientOwnsProject(clientUserId, phase.getProject().getId());

    if (phase.getStatus() != PhaseStatus.DELIVERED) {
      throw new BusinessException(ExceptionConstants.PHASE_WRONG_STATUS);
    }

    if (phase.getRevisionsUsed() >= phase.getMaxRevisions()) {
      throw new BusinessException(ExceptionConstants.NO_REVISIONS_LEFT);
    }

    phase.setRevisionsUsed(phase.getRevisionsUsed() + 1);
    phase.setStatus(PhaseStatus.IN_PROGRESS);
    projectPhaseRepository.save(phase);

    log(
        phase,
        clientUserId,
        PhaseActorType.CLIENT,
        "REVISION_REQUESTED",
        PhaseStatus.DELIVERED,
        PhaseStatus.IN_PROGRESS,
        Map.of(
            "revisionsUsed",
            String.valueOf(phase.getRevisionsUsed()),
            "maxRevisions",
            String.valueOf(phase.getMaxRevisions())));

    PhasePayment payment = phasePaymentRepository.findByProjectPhaseId(phaseId).orElse(null);
    List<PhaseDeliverable> deliverables =
        phaseDeliverableRepository.findByPhaseIdOrderByUploadedAtAsc(phaseId);
    return toPhaseResponse(phase, payment, deliverables);
  }

  @Transactional
  public PhaseResponse approveDeliverable(Long phaseId, Long clientUserId) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyClientOwnsProject(clientUserId, phase.getProject().getId());

    if (phase.getStatus() != PhaseStatus.DELIVERED) {
      throw new BusinessException(ExceptionConstants.PHASE_WRONG_STATUS);
    }

    phase.setStatus(PhaseStatus.APPROVED);
    projectPhaseRepository.save(phase);

    log(
        phase,
        clientUserId,
        PhaseActorType.CLIENT,
        "DELIVERABLE_APPROVED",
        PhaseStatus.DELIVERED,
        PhaseStatus.APPROVED,
        null);

    PhasePayment payment = phasePaymentRepository.findByProjectPhaseId(phaseId).orElse(null);
    List<PhaseDeliverable> deliverables =
        phaseDeliverableRepository.findByPhaseIdOrderByUploadedAtAsc(phaseId);
    return toPhaseResponse(phase, payment, deliverables);
  }

  @Transactional
  public PhaseResponse disputeDeliverable(Long phaseId, Long clientUserId, String reason) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyClientOwnsProject(clientUserId, phase.getProject().getId());

    if (phase.getStatus() != PhaseStatus.DELIVERED) {
      throw new BusinessException(ExceptionConstants.PHASE_WRONG_STATUS);
    }

    phase.setStatus(PhaseStatus.DISPUTED);
    projectPhaseRepository.save(phase);

    log(
        phase,
        clientUserId,
        PhaseActorType.CLIENT,
        "DELIVERABLE_DISPUTED",
        PhaseStatus.DELIVERED,
        PhaseStatus.DISPUTED,
        Map.of("reason", reason));

    PhasePayment payment = phasePaymentRepository.findByProjectPhaseId(phaseId).orElse(null);
    List<PhaseDeliverable> deliverables =
        phaseDeliverableRepository.findByPhaseIdOrderByUploadedAtAsc(phaseId);
    return toPhaseResponse(phase, payment, deliverables);
  }

  @Transactional
  public DisbursementResponse initiateDisbursement(
      Long phaseId, Long architectUserId, DisbursementRequest req) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyArchitectOwnsProject(architectUserId, phase.getProject().getId());

    if (phase.getStatus() != PhaseStatus.APPROVED) {
      throw new BusinessException(ExceptionConstants.PHASE_WRONG_STATUS);
    }

    Architect architect =
        architectRepository
            .findByUserId(architectUserId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS));

    String referenceId = String.format("phase_payout_%d_%d", phaseId, System.currentTimeMillis());

    XenditPayoutRequest.ChannelProperties channelProps =
        XenditPayoutRequest.ChannelProperties.builder()
            .accountNumber(req.getAccountNumber())
            .accountHolderName(req.getAccountHolderName())
            .build();

    XenditPayoutRequest payoutReq =
        XenditPayoutRequest.builder()
            .referenceId(referenceId)
            .channelCode(req.getChannelCode())
            .channelProperties(channelProps)
            .amount(phase.getAmount())
            .currency("IDR")
            .description(
                String.format("Payout for Phase %d: %s", phase.getPhaseNumber(), phase.getTitle()))
            .build();

    XenditPayoutResponse xenditResp;
    try {
      xenditResp = xenditService.createPayout(payoutReq, referenceId);
    } catch (Exception e) {
      log.error("Xendit payout creation failed for phase {}: {}", phaseId, e.getMessage());
      throw new BusinessException(ExceptionConstants.PAYOUT_FAILED);
    }

    PhaseDisbursement disbursement =
        PhaseDisbursement.builder()
            .phase(phase)
            .architect(architect)
            .xenditPayoutId(xenditResp.getId())
            .xenditReferenceId(referenceId)
            .channelCode(req.getChannelCode())
            .accountNumber(req.getAccountNumber())
            .accountHolderName(req.getAccountHolderName())
            .amount(phase.getAmount())
            .status(DisbursementStatus.ACCEPTED)
            .initiatedAt(LocalDateTime.now())
            .build();
    disbursement = phaseDisbursementRepository.save(disbursement);

    log(
        phase,
        architectUserId,
        PhaseActorType.ARCHITECT,
        "PAYOUT_INITIATED",
        PhaseStatus.APPROVED,
        PhaseStatus.APPROVED,
        null);

    return toDisbursementResponse(disbursement);
  }

  @Transactional
  public void handlePayoutCallback(XenditPayoutCallback payload) {
    String payoutId = payload.getId();
    String referenceId = payload.getReferenceId();

    PhaseDisbursement disbursement =
        phaseDisbursementRepository
            .findByXenditPayoutId(payoutId)
            .or(() -> phaseDisbursementRepository.findByXenditReferenceId(referenceId))
            .orElse(null);

    if (disbursement == null) {
      log.info("No disbursement found for payout id={} ref={}", payoutId, referenceId);
      return;
    }

    if (disbursement.getStatus() == DisbursementStatus.SUCCEEDED) {
      log.info("Disbursement already succeeded (idempotent): {}", disbursement.getId());
      return;
    }

    String event = payload.getEvent();
    ProjectPhase phase = disbursement.getPhase();

    if ("payout.succeeded".equals(event)) {
      disbursement.setStatus(DisbursementStatus.SUCCEEDED);
      disbursement.setCompletedAt(LocalDateTime.now());
      phaseDisbursementRepository.save(disbursement);

      phase.setStatus(PhaseStatus.DISBURSED);
      projectPhaseRepository.save(phase);

      log(
          phase,
          null,
          PhaseActorType.XENDIT,
          "PAYOUT_COMPLETED",
          PhaseStatus.APPROVED,
          PhaseStatus.DISBURSED,
          null);

      advanceProjectOrClose(phase.getProject().getId(), phase.getPhaseNumber());

    } else if ("payout.failed".equals(event) || "payout.reversed".equals(event)) {
      DisbursementStatus newStatus =
          "payout.reversed".equals(event) ? DisbursementStatus.REVERSED : DisbursementStatus.FAILED;
      disbursement.setStatus(newStatus);
      disbursement.setFailureCode(payload.getFailureCode());
      phaseDisbursementRepository.save(disbursement);

      log(
          phase,
          null,
          PhaseActorType.XENDIT,
          "PAYOUT_FAILED",
          PhaseStatus.APPROVED,
          PhaseStatus.APPROVED,
          Map.of("failureCode", payload.getFailureCode() != null ? payload.getFailureCode() : ""));
    }
  }

  public List<PhaseResponse> listPhases(Long projectId, Long userId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_NOT_FOUND));

    verifyProjectParticipant(userId, project);

    List<ProjectPhase> phases =
        projectPhaseRepository.findByProjectIdOrderByPhaseNumberAsc(projectId);

    return phases.stream()
        .map(
            phase -> {
              PhasePayment payment =
                  phasePaymentRepository.findByProjectPhaseId(phase.getId()).orElse(null);
              List<PhaseDeliverable> deliverables =
                  phaseDeliverableRepository.findByPhaseIdOrderByUploadedAtAsc(phase.getId());
              return toPhaseResponse(phase, payment, deliverables);
            })
        .collect(Collectors.toList());
  }

  public PhaseResponse getPhase(Long phaseId, Long userId) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyProjectParticipant(userId, phase.getProject());

    PhasePayment payment = phasePaymentRepository.findByProjectPhaseId(phaseId).orElse(null);
    List<PhaseDeliverable> deliverables =
        phaseDeliverableRepository.findByPhaseIdOrderByUploadedAtAsc(phaseId);
    return toPhaseResponse(phase, payment, deliverables);
  }

  public List<PhaseLogResponse> listLogs(Long phaseId, Long userId) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyProjectParticipant(userId, phase.getProject());

    return phaseProcessLogRepository.findByPhaseIdOrderByCreatedAtAsc(phaseId).stream()
        .map(this::toLogResponse)
        .collect(Collectors.toList());
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private void advanceProjectOrClose(Long projectId, int completedPhaseNumber) {
    projectPhaseRepository
        .findFirstByProjectIdAndStatusOrderByPhaseNumberAsc(projectId, PhaseStatus.PENDING)
        .filter(next -> next.getPhaseNumber() > completedPhaseNumber)
        .ifPresentOrElse(
            nextPhase -> {
              log.info("Auto-billing next phase {} for project {}", nextPhase.getId(), projectId);
              Project project = nextPhase.getProject();
              Long clientUserId = project.getClient().getUser().getId();
              createInvoiceForPhase(nextPhase.getId(), clientUserId);
            },
            () -> {
              Project project =
                  projectRepository
                      .findById(projectId)
                      .orElseThrow(
                          () -> new BusinessException(ExceptionConstants.PROJECT_NOT_FOUND));
              project.setStatus(ProjectStatus.COMPLETED);
              projectRepository.save(project);
              log.info("Project {} completed — all phases disbursed", projectId);
            });
  }

  private void log(
      ProjectPhase phase,
      Long actorUserId,
      PhaseActorType actorType,
      String action,
      PhaseStatus from,
      PhaseStatus to,
      Map<String, Object> meta) {
    User actor = null;
    if (actorUserId != null) {
      actor = userRepository.findById(actorUserId).orElse(null);
    }
    PhaseProcessLog entry =
        PhaseProcessLog.builder()
            .phase(phase)
            .actor(actor)
            .actorType(actorType)
            .action(action)
            .fromStatus(from != null ? from.name() : null)
            .toStatus(to != null ? to.name() : null)
            .metadata(meta)
            .build();
    phaseProcessLogRepository.save(entry);
  }

  private void verifyClientOwnsProject(Long clientUserId, Long projectId) {
    Client client =
        clientRepository
            .findByUserId(clientUserId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS));
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_NOT_FOUND));
    if (!project.getClient().getId().equals(client.getId())) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS);
    }
  }

  private void verifyArchitectOwnsProject(Long architectUserId, Long projectId) {
    List<Bid> acceptedBids = bidRepository.findByProjectIdAndStatus(projectId, BidStatus.ACCEPTED);
    boolean isArchitect =
        acceptedBids.stream()
            .anyMatch(b -> b.getArchitect().getUser().getId().equals(architectUserId));
    if (!isArchitect) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS);
    }
  }

  private void verifyProjectParticipant(Long userId, Project project) {
    boolean isClient = project.getClient().getUser().getId().equals(userId);
    if (isClient) return;

    List<Bid> acceptedBids =
        bidRepository.findByProjectIdAndStatus(project.getId(), BidStatus.ACCEPTED);
    boolean isArchitect =
        acceptedBids.stream().anyMatch(b -> b.getArchitect().getUser().getId().equals(userId));
    if (!isArchitect) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS);
    }
  }

  private PhaseResponse toPhaseResponse(
      ProjectPhase phase, PhasePayment payment, List<PhaseDeliverable> deliverables) {
    return PhaseResponse.builder()
        .id(phase.getId())
        .projectId(phase.getProject().getId())
        .phaseNumber(phase.getPhaseNumber())
        .title(phase.getTitle())
        .description(phase.getDescription())
        .amount(phase.getAmount())
        .status(phase.getStatus().name())
        .dueDate(phase.getDueDate())
        .paymentStatus(payment != null ? payment.getStatus().name() : null)
        .paymentLink(payment != null ? payment.getPaymentLink() : null)
        .maxRevisions(phase.getMaxRevisions())
        .revisionsUsed(phase.getRevisionsUsed())
        .deliverables(
            deliverables.stream().map(this::toDeliverableResponse).collect(Collectors.toList()))
        .createdAt(phase.getCreatedAt())
        .updatedAt(phase.getUpdatedAt())
        .build();
  }

  private DeliverableResponse toDeliverableResponse(PhaseDeliverable d) {
    return DeliverableResponse.builder()
        .id(d.getId())
        .filePath(d.getFilePath())
        .fileType(d.getFileType())
        .description(d.getDescription())
        .uploadedAt(d.getUploadedAt())
        .build();
  }

  private DisbursementResponse toDisbursementResponse(PhaseDisbursement d) {
    return DisbursementResponse.builder()
        .id(d.getId())
        .phaseId(d.getPhase().getId())
        .xenditPayoutId(d.getXenditPayoutId())
        .xenditReferenceId(d.getXenditReferenceId())
        .channelCode(d.getChannelCode())
        .accountNumber(d.getAccountNumber())
        .accountHolderName(d.getAccountHolderName())
        .amount(d.getAmount())
        .status(d.getStatus().name())
        .failureCode(d.getFailureCode())
        .initiatedAt(d.getInitiatedAt())
        .completedAt(d.getCompletedAt())
        .build();
  }

  private PhaseLogResponse toLogResponse(PhaseProcessLog l) {
    return PhaseLogResponse.builder()
        .actorType(l.getActorType().name())
        .action(l.getAction())
        .fromStatus(l.getFromStatus())
        .toStatus(l.getToStatus())
        .createdAt(l.getCreatedAt())
        .build();
  }
}
