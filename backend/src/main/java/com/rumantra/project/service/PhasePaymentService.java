package com.rumantra.project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidPaymentPhase;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidPaymentPhaseRepository;
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
import com.rumantra.ledger.service.StatusTransitionService;
import com.rumantra.payment.domain.PhasePayment;
import com.rumantra.payment.domain.PhasePaymentStatus;
import com.rumantra.payment.repository.PhasePaymentRepository;
import com.rumantra.project.domain.DisbursementStatus;
import com.rumantra.project.domain.PhaseActorType;
import com.rumantra.project.domain.PhaseDeliverable;
import com.rumantra.project.domain.PhaseDeliverableApproval;
import com.rumantra.project.domain.PhaseDeliverableRevision;
import com.rumantra.project.domain.PhaseDisbursement;
import com.rumantra.project.domain.PhaseProcessLog;
import com.rumantra.project.domain.PhaseStatus;
import com.rumantra.project.domain.ProjectPhase;
import com.rumantra.project.dto.DeliverableItemResponse;
import com.rumantra.project.dto.DeliverableResponse;
import com.rumantra.project.dto.DeliverableRevisionResponse;
import com.rumantra.project.dto.DeliverableUploadRequest;
import com.rumantra.project.dto.DisbursementRequest;
import com.rumantra.project.dto.DisbursementResponse;
import com.rumantra.project.dto.PhaseCreateRequest;
import com.rumantra.project.dto.PhaseLogResponse;
import com.rumantra.project.dto.PhaseResponse;
import com.rumantra.project.dto.RevisionRequest;
import com.rumantra.project.event.RevisionRequestedEvent;
import com.rumantra.project.repository.PhaseDeliverableApprovalRepository;
import com.rumantra.project.repository.PhaseDeliverableRepository;
import com.rumantra.project.repository.PhaseDeliverableRevisionRepository;
import com.rumantra.project.repository.PhaseDisbursementRepository;
import com.rumantra.project.repository.PhaseProcessLogRepository;
import com.rumantra.project.repository.ProjectPhaseRepository;
import com.rumantra.shared.domain.ActorType;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PhasePaymentService {

  @Autowired private ProjectPhaseRepository projectPhaseRepository;
  @Autowired private StatusTransitionService statusTransitionService;
  @Autowired private PhaseDisbursementRepository phaseDisbursementRepository;
  @Autowired private PhaseDeliverableRepository phaseDeliverableRepository;
  @Autowired private PhaseProcessLogRepository phaseProcessLogRepository;
  @Autowired private PhasePaymentRepository phasePaymentRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private ClientRepository clientRepository;
  @Autowired private ArchitectRepository architectRepository;
  @Autowired private BidRepository bidRepository;
  @Autowired private BidPaymentPhaseRepository bidPaymentPhaseRepository;
  @Autowired private PhaseDeliverableApprovalRepository phaseDeliverableApprovalRepository;
  @Autowired private PhaseDeliverableRevisionRepository phaseDeliverableRevisionRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private XenditService xenditService;
  @Autowired private com.rumantra.shared.storage.FileStorageService fileStorageService;
  @Autowired private org.springframework.context.ApplicationEventPublisher eventPublisher;

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
            .successRedirectUrl(frontendUrl + "/client/projects/" + project.getId() + "/workspace")
            .failureRedirectUrl(frontendUrl + "/client/projects/" + project.getId() + "/workspace")
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
                  return statusTransitionService.transitionPhasePayment(
                      existing,
                      PhasePaymentStatus.PENDING,
                      statusTransitionService.actorRef(clientUserId),
                      ActorType.CLIENT,
                      "INVOICE_REISSUED",
                      null);
                })
            .orElseGet(
                () -> {
                  List<Bid> acceptedBids =
                      bidRepository.findByProjectIdAndStatus(project.getId(), BidStatus.ACCEPTED);
                  Architect architect =
                      acceptedBids.isEmpty() ? null : acceptedBids.get(0).getArchitect();

                  PhasePayment created =
                      phasePaymentRepository.save(
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

                  // Record the invoice itself, not just later status changes -- otherwise the
                  // first event in a payment's life is missing from the ledger entirely.
                  return statusTransitionService.transitionPhasePayment(
                      created,
                      PhasePaymentStatus.PENDING,
                      statusTransitionService.actorRef(clientUserId),
                      ActorType.CLIENT,
                      "INVOICE_CREATED",
                      null);
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

    payment.setXenditInvoiceId(webhook.getId());
    payment.setPaymentMethod(webhook.getPaymentMethod());
    payment.setPaymentChannel(webhook.getPaymentChannel());
    payment.setCompletedAt(LocalDateTime.now());
    statusTransitionService.transitionPhasePayment(
        payment, PhasePaymentStatus.COMPLETED, null, ActorType.XENDIT, "PAYMENT_RECEIVED", null);

    ProjectPhase phase = payment.getProjectPhase();
    PhaseStatus prev = phase.getStatus();
    phase.setStatus(PhaseStatus.IN_PROGRESS);
    // The delivery window opens on payment, not on contract signing: the architect cannot be held
    // to a deadline for work the client has not funded yet.
    bidPhaseFor(phase)
        .map(BidPaymentPhase::getEstimatedDays)
        .filter(days -> days != null && days > 0)
        .ifPresent(days -> phase.setDueDate(LocalDate.now().plusDays(days)));
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
  public void handleInvoiceExpired(XenditInvoiceWebhook webhook) {
    String externalId = webhook.getExternalId();

    PhasePayment payment =
        phasePaymentRepository
            .findByXenditReferenceId(externalId)
            .filter(p -> p.getProjectPhase() != null)
            .orElse(null);

    if (payment == null) {
      log.info("No project-phase payment found for expired invoice: {}", externalId);
      return;
    }

    if (payment.getStatus() != PhasePaymentStatus.PENDING) {
      log.info("Skipping expired webhook for non-pending payment: {}", payment.getId());
      return;
    }

    statusTransitionService.transitionPhasePayment(
        payment, PhasePaymentStatus.EXPIRED, null, ActorType.XENDIT, "INVOICE_EXPIRED", null);

    ProjectPhase phase = payment.getProjectPhase();
    PhaseStatus prev = phase.getStatus();
    phase.setStatus(PhaseStatus.PENDING);
    projectPhaseRepository.save(phase);

    log(phase, null, PhaseActorType.XENDIT, "INVOICE_EXPIRED", prev, PhaseStatus.PENDING, null);

    log.info("Project phase {} reset to PENDING after invoice expiry", phase.getId());
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

    if (req.getFilePath() == null || !req.getFilePath().matches("^https?://.+")) {
      throw new IllegalArgumentException("filePath must be an absolute http(s) URL");
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
            .deliverableIndex(req.getDeliverableIndex())
            .revisionRound(phase.getRevisionsUsed())
            .build();
    deliverable = phaseDeliverableRepository.save(deliverable);

    log(phase, architectUserId, PhaseActorType.ARCHITECT, "DELIVERABLE_UPLOADED", null, null, null);

    maybeAutoDeliver(phase, architectUserId);

    return toDeliverableResponse(deliverable);
  }

  @Transactional
  public DeliverableResponse uploadDeliverableFile(
      Long phaseId,
      Long architectUserId,
      org.springframework.web.multipart.MultipartFile file,
      String description,
      Integer deliverableIndex) {
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
            .deliverableIndex(deliverableIndex)
            .revisionRound(phase.getRevisionsUsed())
            .build();
    deliverable = phaseDeliverableRepository.save(deliverable);

    log(phase, architectUserId, PhaseActorType.ARCHITECT, "DELIVERABLE_UPLOADED", null, null, null);

    maybeAutoDeliver(phase, architectUserId);

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

    deliverPhase(phase, architectUserId);

    PhasePayment payment = phasePaymentRepository.findByProjectPhaseId(phaseId).orElse(null);
    return toPhaseResponse(phase, payment, deliverables);
  }

  private void deliverPhase(ProjectPhase phase, Long architectUserId) {
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
  }

  /**
   * Delivery is the act of completing the deliverable list, not a separate button: once every
   * deliverable named in the bid is either approved or answered for the current revision round, the
   * phase goes to the client for review on its own.
   *
   * <p>A phase whose bid named no deliverables has no list to complete, so it keeps the explicit
   * submit-for-review path instead.
   */
  private void maybeAutoDeliver(ProjectPhase phase, Long architectUserId) {
    if (deliverableNamesForPhase(phase).isEmpty()) {
      return;
    }
    List<PhaseDeliverable> files =
        phaseDeliverableRepository.findByPhaseIdOrderByUploadedAtAsc(phase.getId());
    boolean outstanding =
        toDeliverableItems(phase, files).stream()
            .filter(item -> item.getIndex() != null)
            .anyMatch(
                item ->
                    "MISSING".equals(item.getStatus())
                        || "REVISION_REQUESTED".equals(item.getStatus()));
    if (!outstanding) {
      deliverPhase(phase, architectUserId);
    }
  }

  /**
   * Sends selected deliverables back for rework. The instructions are per deliverable but the cost
   * is not: everything in one request shares a revision round, so asking for five changes at once
   * spends the same single round as asking for one.
   */
  @Transactional
  public PhaseResponse requestRevision(
      Long phaseId, Long clientUserId, List<RevisionRequest.Item> items) {
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

    List<String> names = deliverableNamesForPhase(phase);
    if (items == null || items.isEmpty()) {
      throw new BusinessException(ExceptionConstants.INVALID_REVISION_REQUEST);
    }
    Set<Integer> seen = new LinkedHashSet<>();
    for (RevisionRequest.Item item : items) {
      Integer index = item.getIndex();
      if (index == null || index < 0 || index >= names.size() || !seen.add(index)) {
        throw new BusinessException(ExceptionConstants.INVALID_REVISION_REQUEST);
      }
      if (item.getNotes() == null || item.getNotes().isBlank()) {
        throw new BusinessException(ExceptionConstants.INVALID_REVISION_REQUEST);
      }
    }

    phase.setRevisionsUsed(phase.getRevisionsUsed() + 1);
    int round = phase.getRevisionsUsed();

    // Only what was sent back loses its approval: a deliverable the client already accepted and
    // did not ask to change stays accepted, so it does not have to be re-approved next round.
    phaseDeliverableApprovalRepository.deleteByPhaseIdAndDeliverableIndexIn(
        phaseId, new ArrayList<>(seen));

    phase.setStatus(PhaseStatus.IN_PROGRESS);
    projectPhaseRepository.save(phase);

    User requester = userRepository.findById(clientUserId).orElse(null);
    for (RevisionRequest.Item item : items) {
      phaseDeliverableRevisionRepository.save(
          PhaseDeliverableRevision.builder()
              .phase(phase)
              .deliverableIndex(item.getIndex())
              .revisionRound(round)
              .notes(item.getNotes().trim())
              .requestedBy(requester)
              .build());
    }

    // The notification and the log entry carry one readable string; the per-deliverable rows above
    // are what the workspace renders against each row.
    String notes =
        items.stream()
            .map(i -> names.get(i.getIndex()) + ": " + i.getNotes().trim())
            .collect(Collectors.joining("; "));

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
            String.valueOf(phase.getMaxRevisions()),
            "deliverableIndexes",
            seen.stream().map(String::valueOf).collect(Collectors.joining(",")),
            "notes",
            notes));

    List<Bid> bids =
        bidRepository.findByProjectIdAndStatus(phase.getProject().getId(), BidStatus.ACCEPTED);
    if (!bids.isEmpty()) {
      Long architectUserId = bids.get(0).getArchitect().getUser().getId();
      eventPublisher.publishEvent(
          new RevisionRequestedEvent(
              this,
              phaseId,
              phase.getProject().getId(),
              architectUserId,
              clientUserId,
              phase.getProject().getTitle(),
              phase.getTitle(),
              phase.getRevisionsUsed(),
              phase.getMaxRevisions(),
              notes));
    }

    PhasePayment payment = phasePaymentRepository.findByProjectPhaseId(phaseId).orElse(null);
    List<PhaseDeliverable> deliverables =
        phaseDeliverableRepository.findByPhaseIdOrderByUploadedAtAsc(phaseId);
    return toPhaseResponse(phase, payment, deliverables);
  }

  @Transactional
  /**
   * The deliverable names for a phase, read from the accepted bid rather than copied onto the phase
   * — the bid remains the single source of truth for what was contracted.
   */
  public List<String> deliverableNamesForPhase(ProjectPhase phase) {
    return bidPhaseFor(phase)
        .map(bp -> bp.getDeliverables() == null ? List.<String>of() : bp.getDeliverables())
        .orElseGet(List::of);
  }

  /** The bid phase a project phase was created from, matched on phase number. */
  private Optional<BidPaymentPhase> bidPhaseFor(ProjectPhase phase) {
    List<Bid> accepted =
        bidRepository.findByProjectIdAndStatus(phase.getProject().getId(), BidStatus.ACCEPTED);
    if (accepted.isEmpty()) {
      return Optional.empty();
    }
    return bidPaymentPhaseRepository.findByBidIdOrderByPhaseNumber(accepted.get(0).getId()).stream()
        .filter(bp -> Objects.equals(bp.getPhaseNumber(), phase.getPhaseNumber()))
        .findFirst();
  }

  /**
   * Approves a single deliverable. When every deliverable named in the bid has been approved the
   * phase itself is approved, which is what makes the architect's payout available.
   */
  @Transactional
  public PhaseResponse approveDeliverableItem(Long phaseId, Integer index, Long clientUserId) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyClientOwnsProject(clientUserId, phase.getProject().getId());

    if (phase.getStatus() != PhaseStatus.DELIVERED) {
      throw new BusinessException(ExceptionConstants.PHASE_WRONG_STATUS);
    }

    List<String> names = deliverableNamesForPhase(phase);
    if (index == null || index < 0 || index >= names.size()) {
      throw new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND);
    }

    // The unique constraint carries the concurrency guarantee, so a repeat approval is simply a
    // no-op rather than a second row or an error the client has to handle.
    if (!phaseDeliverableApprovalRepository.existsByPhaseIdAndDeliverableIndex(phaseId, index)) {
      phaseDeliverableApprovalRepository.save(
          PhaseDeliverableApproval.builder()
              .phase(phase)
              .deliverableIndex(index)
              .approvedBy(userRepository.getReferenceById(clientUserId))
              .build());

      log(
          phase,
          clientUserId,
          PhaseActorType.CLIENT,
          "DELIVERABLE_APPROVED",
          phase.getStatus(),
          phase.getStatus(),
          Map.of("deliverableIndex", String.valueOf(index), "deliverableName", names.get(index)));
    }

    if (phaseDeliverableApprovalRepository.countByPhaseId(phaseId) >= names.size()) {
      return approveDeliverable(phaseId, clientUserId);
    }

    PhasePayment payment = phasePaymentRepository.findByProjectPhaseId(phaseId).orElse(null);
    List<PhaseDeliverable> deliverables =
        phaseDeliverableRepository.findByPhaseIdOrderByUploadedAtAsc(phaseId);
    return toPhaseResponse(phase, payment, deliverables);
  }

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
    // Lock the phase for this transaction: initiating a payout deliberately leaves the phase
    // APPROVED until the webhook lands, so the status guard below stays open in the meantime and
    // cannot by itself stop a second request creating a second Xendit payout.
    ProjectPhase phase =
        projectPhaseRepository
            .findWithLockById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    verifyArchitectOwnsProject(architectUserId, phase.getProject().getId());

    if (phase.getStatus() != PhaseStatus.APPROVED) {
      throw new BusinessException(ExceptionConstants.PHASE_WRONG_STATUS);
    }

    // A payout that already exists and has not failed is still live; only FAILED/REVERSED may be
    // retried. The partial unique index on rmtr_project_phase_disbursement backs this up.
    if (!phaseDisbursementRepository
        .findByPhaseIdAndStatusNotIn(
            phaseId, List.of(DisbursementStatus.FAILED, DisbursementStatus.REVERSED))
        .isEmpty()) {
      throw new BusinessException(ExceptionConstants.PAYOUT_ALREADY_REQUESTED);
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
            .status(DisbursementStatus.PENDING)
            .initiatedAt(LocalDateTime.now())
            .build();
    disbursement = phaseDisbursementRepository.save(disbursement);
    disbursement =
        statusTransitionService.transitionDisbursement(
            disbursement,
            DisbursementStatus.ACCEPTED,
            statusTransitionService.actorRef(architectUserId),
            ActorType.ARCHITECT,
            "PAYOUT_INITIATED",
            null);

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
      disbursement.setCompletedAt(LocalDateTime.now());
      statusTransitionService.transitionDisbursement(
          disbursement,
          DisbursementStatus.SUCCEEDED,
          null,
          ActorType.XENDIT,
          "PAYOUT_COMPLETED",
          null);

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
      disbursement.setFailureCode(payload.getFailureCode());
      statusTransitionService.transitionDisbursement(
          disbursement,
          newStatus,
          null,
          ActorType.XENDIT,
          "PAYOUT_FAILED",
          payload.getFailureCode() == null
              ? null
              : Map.of("failureCode", payload.getFailureCode()));

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
              statusTransitionService.transitionProject(
                  project,
                  ProjectStatus.COMPLETED,
                  null,
                  ActorType.SYSTEM,
                  "PROJECT_COMPLETED",
                  null);
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
    PhaseDisbursement disbursement =
        phaseDisbursementRepository.findByPhaseId(phase.getId()).orElse(null);
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
        .disbursementStatus(disbursement != null ? disbursement.getStatus().name() : null)
        .deliverables(
            deliverables.stream().map(this::toDeliverableResponse).collect(Collectors.toList()))
        .deliverableItems(toDeliverableItems(phase, deliverables))
        .createdAt(phase.getCreatedAt())
        .updatedAt(phase.getUpdatedAt())
        .build();
  }

  /**
   * Groups a phase's files under the deliverables named in the accepted bid. Files uploaded before
   * tagging existed carry a null index and are collected under a trailing "Other files" entry so
   * nothing disappears from history.
   */
  private List<DeliverableItemResponse> toDeliverableItems(
      ProjectPhase phase, List<PhaseDeliverable> files) {
    List<String> names = deliverableNamesForPhase(phase);
    Set<Integer> approved =
        phaseDeliverableApprovalRepository.findByPhaseId(phase.getId()).stream()
            .map(PhaseDeliverableApproval::getDeliverableIndex)
            .collect(Collectors.toSet());

    Map<Integer, List<PhaseDeliverableRevision>> revisionsByIndex =
        phaseDeliverableRevisionRepository
            .findByPhaseIdOrderByRevisionRoundAscDeliverableIndexAsc(phase.getId())
            .stream()
            .collect(Collectors.groupingBy(PhaseDeliverableRevision::getDeliverableIndex));

    boolean phaseNotOpen =
        phase.getStatus() == PhaseStatus.PENDING || phase.getStatus() == PhaseStatus.BILLED;

    List<DeliverableItemResponse> items = new ArrayList<>();
    for (int i = 0; i < names.size(); i++) {
      final int index = i;
      List<DeliverableResponse> tagged =
          files.stream()
              .filter(f -> Objects.equals(f.getDeliverableIndex(), index))
              .map(this::toDeliverableResponse)
              .collect(Collectors.toList());

      List<PhaseDeliverableRevision> revisions = revisionsByIndex.getOrDefault(index, List.of());

      // A revision is outstanding until the architect answers it with an upload in that same
      // round, which is what the file's revisionRound records.
      boolean revisionOutstanding =
          revisions.stream()
                  .anyMatch(r -> Objects.equals(r.getRevisionRound(), phase.getRevisionsUsed()))
              && tagged.stream()
                  .noneMatch(f -> Objects.equals(f.getRevisionRound(), phase.getRevisionsUsed()));

      String status;
      if (approved.contains(index)) {
        status = "APPROVED";
      } else if (phaseNotOpen) {
        status = "LOCKED";
      } else if (revisionOutstanding) {
        status = "REVISION_REQUESTED";
      } else if (tagged.isEmpty()) {
        status = "MISSING";
      } else {
        status = "PENDING";
      }

      items.add(
          DeliverableItemResponse.builder()
              .index(index)
              .name(names.get(index))
              .status(status)
              .files(tagged)
              .revisions(
                  revisions.stream()
                      .map(
                          r ->
                              DeliverableRevisionResponse.builder()
                                  .round(r.getRevisionRound())
                                  .notes(r.getNotes())
                                  .requestedAt(r.getRequestedAt())
                                  .build())
                      .collect(Collectors.toList()))
              .build());
    }

    List<DeliverableResponse> untagged =
        files.stream()
            .filter(f -> f.getDeliverableIndex() == null)
            .map(this::toDeliverableResponse)
            .collect(Collectors.toList());
    if (!untagged.isEmpty()) {
      items.add(
          DeliverableItemResponse.builder()
              .index(null)
              .name(null)
              .status("PENDING")
              .files(untagged)
              .revisions(List.of())
              .build());
    }
    return items;
  }

  private DeliverableResponse toDeliverableResponse(PhaseDeliverable d) {
    return DeliverableResponse.builder()
        .id(d.getId())
        .filePath(fileStorageService.getPublicUrl(d.getFilePath()))
        .fileType(d.getFileType())
        .description(d.getDescription())
        .deliverableIndex(d.getDeliverableIndex())
        .revisionRound(d.getRevisionRound())
        .uploadedAt(d.getUploadedAt())
        .build();
  }

  private DisbursementResponse toDisbursementResponse(PhaseDisbursement d) {
    return DisbursementResponse.builder()
        .status(d.getStatus().name())
        .amount(d.getAmount())
        .failureCode(d.getFailureCode())
        .build();
  }

  private PhaseLogResponse toLogResponse(PhaseProcessLog l) {
    return PhaseLogResponse.builder()
        .actorType(l.getActorType().name())
        .action(l.getAction())
        .fromStatus(l.getFromStatus())
        .toStatus(l.getToStatus())
        .metadata(l.getMetadata())
        .createdAt(l.getCreatedAt())
        .build();
  }
}
