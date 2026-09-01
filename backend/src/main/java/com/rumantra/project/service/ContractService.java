package com.rumantra.project.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidPaymentPhase;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidPaymentPhaseRepository;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.Project;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.payment.domain.PhasePayment;
import com.rumantra.payment.domain.PhasePaymentStatus;
import com.rumantra.payment.repository.PhasePaymentRepository;
import com.rumantra.payment.repository.PhasePaymentStatusLogRepository;
import com.rumantra.project.domain.DisbursementStatus;
import com.rumantra.project.domain.ProjectPhase;
import com.rumantra.project.dto.AgreementTermsResponse;
import com.rumantra.project.dto.ContractPhaseResponse;
import com.rumantra.project.dto.ContractResponse;
import com.rumantra.project.dto.TransactionResponse;
import com.rumantra.project.dto.WinningBidResponse;
import com.rumantra.project.repository.PhaseDisbursementStatusLogRepository;
import com.rumantra.project.repository.ProjectPhaseRepository;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.shared.storage.FileStorageService;
import com.rumantra.user.domain.User;

import lombok.RequiredArgsConstructor;

/** Read model for the workspace's Contract &amp; Payment tab. */
@Service
@RequiredArgsConstructor
public class ContractService {

  private final ProjectRepository projectRepository;
  private final ProjectPhaseRepository projectPhaseRepository;
  private final PhasePaymentRepository phasePaymentRepository;
  private final BidRepository bidRepository;
  private final BidPaymentPhaseRepository bidPaymentPhaseRepository;
  private final PhasePaymentStatusLogRepository phasePaymentStatusLogRepository;
  private final PhaseDisbursementStatusLogRepository phaseDisbursementStatusLogRepository;
  private final FileStorageService fileStorageService;

  @Transactional(readOnly = true)
  public ContractResponse getContract(Long projectId, Long userId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_NOT_FOUND));

    verifyParticipant(userId, project);

    Bid bid =
        bidRepository.findByProjectIdAndStatus(projectId, BidStatus.ACCEPTED).stream()
            .findFirst()
            .orElse(null);
    List<BidPaymentPhase> bidPhases =
        bid == null
            ? List.of()
            : bidPaymentPhaseRepository.findByBidIdOrderByPhaseNumber(bid.getId());
    List<ProjectPhase> phases =
        projectPhaseRepository.findByProjectIdOrderByPhaseNumberAsc(projectId);
    Map<Long, PhasePayment> paymentsByPhase =
        phasePaymentRepository.findByProjectIdOrderByCreatedAtAsc(projectId).stream()
            .filter(p -> p.getProjectPhase() != null)
            .collect(
                Collectors.toMap(
                    p -> p.getProjectPhase().getId(), p -> p, (first, second) -> second));

    BigDecimal total =
        phases.stream()
            .map(ProjectPhase::getAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return ContractResponse.builder()
        .projectId(projectId)
        .totalValue(total)
        .paidValue(sumWhere(phases, paymentsByPhase, PhasePaymentStatus.COMPLETED))
        .disbursedValue(disbursedTotal(phases))
        .paymentSchedule(schedule(phases, paymentsByPhase, total))
        .agreementTerms(terms(project, bid, bidPhases))
        .winningBid(winningBid(bid, bidPhases))
        .transactions(transactions(projectId))
        .build();
  }

  private void verifyParticipant(Long userId, Project project) {
    if (project.getClient().getUser().getId().equals(userId)) {
      return;
    }
    boolean isWinningArchitect =
        bidRepository.findByProjectIdAndStatus(project.getId(), BidStatus.ACCEPTED).stream()
            .anyMatch(b -> b.getArchitect().getUser().getId().equals(userId));
    if (!isWinningArchitect) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_PHASE_ACCESS);
    }
  }

  private BigDecimal sumWhere(
      List<ProjectPhase> phases, Map<Long, PhasePayment> payments, PhasePaymentStatus status) {
    return phases.stream()
        .filter(
            p -> {
              PhasePayment payment = payments.get(p.getId());
              return payment != null && payment.getStatus() == status;
            })
        .map(ProjectPhase::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal disbursedTotal(List<ProjectPhase> phases) {
    return phases.stream()
        .filter(p -> p.getStatus() == com.rumantra.project.domain.PhaseStatus.DISBURSED)
        .map(ProjectPhase::getAmount)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private List<ContractPhaseResponse> schedule(
      List<ProjectPhase> phases, Map<Long, PhasePayment> payments, BigDecimal total) {
    return phases.stream()
        .map(
            phase -> {
              PhasePayment payment = payments.get(phase.getId());
              BigDecimal share =
                  total == null || total.signum() == 0 || phase.getAmount() == null
                      ? BigDecimal.ZERO
                      : phase
                          .getAmount()
                          .multiply(BigDecimal.valueOf(100))
                          .divide(total, 1, RoundingMode.HALF_UP);
              return ContractPhaseResponse.builder()
                  .phaseId(phase.getId())
                  .phaseNumber(phase.getPhaseNumber())
                  .title(phase.getTitle())
                  .amount(phase.getAmount())
                  .share(share)
                  .dueDate(phase.getDueDate())
                  .status(phase.getStatus().name())
                  .paymentStatus(payment == null ? null : payment.getStatus().name())
                  .build();
            })
        .collect(Collectors.toList());
  }

  private AgreementTermsResponse terms(Project project, Bid bid, List<BidPaymentPhase> bidPhases) {
    return AgreementTermsResponse.builder()
        .scopeOfWork(project.getScopeOfWork())
        .feeTotal(bid == null ? null : bid.getBidAmount())
        .phaseCount(bidPhases.size())
        .revisionsPerPhase(
            bidPhases.stream()
                .map(BidPaymentPhase::getRevisionRounds)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null))
        .timelineDays(bid == null ? null : bid.getProposedTimelineDays())
        .build();
  }

  private WinningBidResponse winningBid(Bid bid, List<BidPaymentPhase> bidPhases) {
    if (bid == null) {
      return null;
    }
    User user = bid.getArchitect().getUser();
    String name =
        java.util.stream.Stream.of(user.getFirstName(), user.getLastName())
            .filter(part -> part != null && !part.isBlank())
            .collect(Collectors.joining(" "));
    return WinningBidResponse.builder()
        .bidId(bid.getId())
        .architectId(bid.getArchitect().getId())
        .architectName(name.isBlank() ? user.getEmail() : name)
        .companyName(bid.getArchitect().getCompanyName())
        .city(bid.getArchitect().getCity())
        .photoUrl(
            bid.getArchitect().getPhotoUrl() == null
                ? null
                : fileStorageService.getPublicUrl(bid.getArchitect().getPhotoUrl()))
        .bidAmount(bid.getBidAmount())
        .timelineDays(bid.getProposedTimelineDays())
        .phaseCount(bidPhases.size())
        .revisionsPerPhase(
            bidPhases.stream()
                .map(BidPaymentPhase::getRevisionRounds)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null))
        .build();
  }

  /**
   * Built from the append-only status ledger, which is the record of what actually happened. The
   * logs carry no amount or Xendit reference, so each row is joined back to its parent for those.
   */
  private List<TransactionResponse> transactions(Long projectId) {
    List<TransactionResponse> rows = new ArrayList<>();

    phasePaymentStatusLogRepository.findByProjectId(projectId).stream()
        .filter(entry -> !"LEDGER_INITIALIZED".equals(entry.getAction()))
        .forEach(
            entry -> {
              PhasePayment payment = entry.getPhasePayment();
              ProjectPhase phase = payment.getProjectPhase();
              rows.add(
                  TransactionResponse.builder()
                      .at(entry.getCreatedAt())
                      .action(entry.getAction())
                      .actorType(entry.getActorType().name())
                      .direction("IN")
                      .amount(payment.getAmount())
                      .reference(payment.getXenditReferenceId())
                      .phaseNumber(phase == null ? null : phase.getPhaseNumber())
                      .phaseTitle(phase == null ? null : phase.getTitle())
                      .build());
            });

    phaseDisbursementStatusLogRepository.findByProjectId(projectId).stream()
        .filter(entry -> !"LEDGER_INITIALIZED".equals(entry.getAction()))
        .forEach(
            entry -> {
              var disbursement = entry.getDisbursement();
              ProjectPhase phase = disbursement.getPhase();
              rows.add(
                  TransactionResponse.builder()
                      .at(entry.getCreatedAt())
                      .action(entry.getAction())
                      .actorType(entry.getActorType().name())
                      .direction(
                          disbursement.getStatus() == DisbursementStatus.REVERSED ? "IN" : "OUT")
                      .amount(disbursement.getAmount())
                      .reference(disbursement.getXenditReferenceId())
                      .phaseNumber(phase == null ? null : phase.getPhaseNumber())
                      .phaseTitle(phase == null ? null : phase.getTitle())
                      .build());
            });

    rows.sort(Comparator.comparing(TransactionResponse::getAt));
    return rows;
  }
}
