package com.rumantra.payment.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidPaymentPhase;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidPaymentPhaseRepository;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.Client;
import com.rumantra.client.domain.Project;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.integration.xendit.XenditService;
import com.rumantra.integration.xendit.dto.XenditInvoiceRequest;
import com.rumantra.integration.xendit.dto.XenditInvoiceResponse;
import com.rumantra.integration.xendit.dto.XenditInvoiceWebhook;
import com.rumantra.ledger.service.StatusTransitionService;
import com.rumantra.payment.domain.PhasePayment;
import com.rumantra.payment.domain.PhasePaymentStatus;
import com.rumantra.payment.dto.PhasePaymentInitiateResponse;
import com.rumantra.payment.dto.PhasePaymentResponse;
import com.rumantra.payment.repository.PhasePaymentRepository;
import com.rumantra.project.domain.PhaseStatus;
import com.rumantra.project.repository.ProjectPhaseRepository;
import com.rumantra.shared.domain.ActorType;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentService {

  @Autowired private PhasePaymentRepository phasePaymentRepository;
  @Autowired private StatusTransitionService statusTransitionService;

  @Autowired private BidPaymentPhaseRepository bidPaymentPhaseRepository;

  @Autowired private BidRepository bidRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private ClientRepository clientRepository;

  @Autowired private XenditService xenditService;

  @Autowired private ProjectPhaseRepository projectPhaseRepository;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  public List<PhasePaymentResponse> getProjectPhasePayments(Long projectId, Long userId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_NOT_FOUND));

    if (!project.getClient().getUser().getId().equals(userId)) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_PROJECT_ACCESS);
    }

    List<Bid> acceptedBids = bidRepository.findByProjectIdAndStatus(projectId, BidStatus.ACCEPTED);
    if (acceptedBids.isEmpty()) {
      return List.of();
    }

    Bid acceptedBid = acceptedBids.get(0);
    List<BidPaymentPhase> phases =
        bidPaymentPhaseRepository.findByBidIdOrderByPhaseNumber(acceptedBid.getId());

    Map<Long, PhasePayment> paymentsByPhaseId =
        phasePaymentRepository.findByProjectIdOrderByCreatedAtAsc(projectId).stream()
            .collect(Collectors.toMap(p -> p.getPhase().getId(), p -> p));

    return phases.stream()
        .map(
            phase -> {
              PhasePayment payment = paymentsByPhaseId.get(phase.getId());
              return PhasePaymentResponse.builder()
                  .phaseId(phase.getId())
                  .phaseNumber(phase.getPhaseNumber())
                  .title(phase.getTitle())
                  .deliverables(phase.getDeliverables())
                  .amount(phase.getAmount())
                  .paymentStatus(payment != null ? payment.getStatus().name() : "PENDING")
                  .paymentLink(payment != null ? payment.getPaymentLink() : null)
                  .paidAt(payment != null ? payment.getCompletedAt() : null)
                  .build();
            })
        .collect(Collectors.toList());
  }

  @Transactional
  public PhasePaymentInitiateResponse initiatePhasePayment(Long phaseId, Long userId) {
    BidPaymentPhase phase =
        bidPaymentPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PHASE_NOT_FOUND));

    Bid bid = phase.getBid();
    Project project = bid.getProject();

    if (!project.getClient().getUser().getId().equals(userId)) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_PROJECT_ACCESS);
    }

    Optional<PhasePayment> existingPayment = phasePaymentRepository.findByPhaseId(phaseId);
    if (existingPayment.isPresent()
        && existingPayment.get().getStatus() == PhasePaymentStatus.COMPLETED) {
      throw new BusinessException(ExceptionConstants.PHASE_ALREADY_PAID);
    }

    Client client = project.getClient();
    String referenceId =
        String.format(
            "phase_payment_proj_%d_phase_%d_%d",
            project.getId(), phaseId, System.currentTimeMillis());

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

    XenditInvoiceRequest.CustomerNotificationPreference notificationPref =
        XenditInvoiceRequest.CustomerNotificationPreference.builder()
            .invoiceCreated(new ArrayList<>())
            .invoiceReminder(new ArrayList<>())
            .invoicePaid(Arrays.asList("email"))
            .build();

    XenditInvoiceRequest xenditRequest =
        XenditInvoiceRequest.builder()
            .externalId(referenceId)
            .amount(phase.getAmount())
            .description(
                String.format(
                    "Payment for Phase %d: %s - Project: %s",
                    phase.getPhaseNumber(), phase.getTitle(), project.getTitle()))
            .currency("IDR")
            .invoiceDuration(86400)
            .successRedirectUrl(frontendUrl + "/client/projects/" + project.getId() + "/workspace")
            .failureRedirectUrl(frontendUrl + "/client/projects/" + project.getId() + "/active")
            .customer(customer)
            .customerNotificationPreference(notificationPref)
            .items(Arrays.asList(item))
            .build();

    XenditInvoiceResponse xenditResponse = xenditService.createInvoice(xenditRequest);

    LocalDateTime expiryDate =
        ZonedDateTime.parse(xenditResponse.getExpiryDate(), DateTimeFormatter.ISO_DATE_TIME)
            .toLocalDateTime();

    PhasePayment phasePayment;
    if (existingPayment.isPresent()) {
      PhasePayment existing = existingPayment.get();
      statusTransitionService.transitionPhasePayment(
          existing, PhasePaymentStatus.PENDING, null, ActorType.CLIENT, "INVOICE_REISSUED", null);
      existing.setXenditInvoiceId(xenditResponse.getId());
      existing.setXenditReferenceId(referenceId);
      existing.setPaymentLink(xenditResponse.getInvoiceUrl());
      existing.setExpiresAt(expiryDate);
      phasePayment = phasePaymentRepository.save(existing);
    } else {
      phasePayment =
          phasePaymentRepository.save(
              PhasePayment.builder()
                  .phase(phase)
                  .project(project)
                  .client(client)
                  .architect(bid.getArchitect())
                  .amount(phase.getAmount())
                  .status(PhasePaymentStatus.PENDING)
                  .xenditInvoiceId(xenditResponse.getId())
                  .xenditReferenceId(referenceId)
                  .paymentLink(xenditResponse.getInvoiceUrl())
                  .expiresAt(expiryDate)
                  .build());
    }

    log.info(
        "Phase payment initiated: phaseId={}, projectId={}, amount={}",
        phaseId,
        project.getId(),
        phase.getAmount());

    return PhasePaymentInitiateResponse.builder()
        .amount(phase.getAmount())
        .paymentLink(phasePayment.getPaymentLink())
        .expiresAt(phasePayment.getExpiresAt())
        .status(phasePayment.getStatus().name())
        .build();
  }

  @Transactional
  public void handleInvoicePaid(XenditInvoiceWebhook webhook) {
    String externalId = webhook.getExternalId();
    PhasePayment payment =
        phasePaymentRepository
            .findByXenditReferenceId(externalId)
            .orElseThrow(
                () -> new RuntimeException("Phase payment not found for reference: " + externalId));

    if (payment.getStatus() == PhasePaymentStatus.COMPLETED) {
      log.info("Phase payment already completed, skipping: {}", payment.getId());
      return;
    }

    payment.setXenditInvoiceId(webhook.getId());
    payment.setPaymentMethod(webhook.getPaymentMethod());
    payment.setPaymentChannel(webhook.getPaymentChannel());

    if (webhook.getPaidAt() != null) {
      payment.setCompletedAt(
          ZonedDateTime.parse(webhook.getPaidAt(), DateTimeFormatter.ISO_DATE_TIME)
              .toLocalDateTime());
    } else {
      payment.setCompletedAt(LocalDateTime.now());
    }

    statusTransitionService.transitionPhasePayment(
        payment, PhasePaymentStatus.COMPLETED, null, ActorType.XENDIT, "PAYMENT_RECEIVED", null);
    log.info(
        "Phase payment completed: phaseId={}, projectId={}",
        payment.getPhase().getId(),
        payment.getProject().getId());

    advanceProjectPhaseToInProgress(payment);
  }

  private void advanceProjectPhaseToInProgress(PhasePayment payment) {
    int phaseNumber = payment.getPhase().getPhaseNumber();
    Long projectId = payment.getProject().getId();
    projectPhaseRepository
        .findByProjectIdAndPhaseNumber(projectId, phaseNumber)
        .ifPresent(
            phase -> {
              if (phase.getStatus() == PhaseStatus.PENDING
                  || phase.getStatus() == PhaseStatus.BILLED) {
                phase.setStatus(PhaseStatus.IN_PROGRESS);
                Integer estimatedDays = payment.getPhase().getEstimatedDays();
                if (estimatedDays != null && estimatedDays > 0) {
                  phase.setDueDate(LocalDate.now().plusDays(estimatedDays));
                }
                projectPhaseRepository.save(phase);
                log.info(
                    "ProjectPhase {} advanced to IN_PROGRESS after BidPaymentPhase payment",
                    phase.getId());
              }
            });
  }

  @Transactional
  public void handleInvoiceExpired(XenditInvoiceWebhook webhook) {
    String externalId = webhook.getExternalId();
    phasePaymentRepository
        .findByXenditReferenceId(externalId)
        .ifPresent(
            payment -> {
              statusTransitionService.transitionPhasePayment(
                  payment,
                  PhasePaymentStatus.EXPIRED,
                  null,
                  ActorType.XENDIT,
                  "INVOICE_EXPIRED",
                  null);
              log.info("Phase payment expired: phaseId={}", payment.getPhase().getId());
            });
  }
}
