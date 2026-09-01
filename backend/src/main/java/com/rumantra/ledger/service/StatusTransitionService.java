package com.rumantra.ledger.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.client.domain.Project;
import com.rumantra.client.domain.ProjectStatus;
import com.rumantra.client.domain.ProjectStatusLog;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.client.repository.ProjectStatusLogRepository;
import com.rumantra.payment.domain.PhasePayment;
import com.rumantra.payment.domain.PhasePaymentStatus;
import com.rumantra.payment.domain.PhasePaymentStatusLog;
import com.rumantra.payment.domain.PurchaseStatus;
import com.rumantra.payment.domain.TokenPurchase;
import com.rumantra.payment.domain.TokenPurchaseStatusLog;
import com.rumantra.payment.repository.PhasePaymentRepository;
import com.rumantra.payment.repository.PhasePaymentStatusLogRepository;
import com.rumantra.payment.repository.TokenPurchaseRepository;
import com.rumantra.payment.repository.TokenPurchaseStatusLogRepository;
import com.rumantra.project.domain.DisbursementStatus;
import com.rumantra.project.domain.PhaseDisbursement;
import com.rumantra.project.domain.PhaseDisbursementStatusLog;
import com.rumantra.project.repository.PhaseDisbursementRepository;
import com.rumantra.project.repository.PhaseDisbursementStatusLogRepository;
import com.rumantra.shared.domain.ActorType;
import com.rumantra.shared.domain.StatusLogEntry;
import com.rumantra.subscription.domain.Subscription;
import com.rumantra.subscription.domain.SubscriptionStatus;
import com.rumantra.subscription.domain.SubscriptionStatusLog;
import com.rumantra.subscription.repository.SubscriptionRepository;
import com.rumantra.subscription.repository.SubscriptionStatusLogRepository;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * The only supported way to change status on a money-touching entity.
 *
 * <p>Each method moves the entity's status column and appends a row to that stream's append-only
 * log in the same transaction, so the two cannot diverge. The log is the source of truth; the
 * status column is a projection of it, kept because it is what authorization reads — a single row
 * that can be locked and constrained.
 *
 * <p>Never call {@code setStatus} directly on these entities.
 */
@Service
@RequiredArgsConstructor
public class StatusTransitionService {

  private final ProjectRepository projectRepository;
  private final ProjectStatusLogRepository projectStatusLogRepository;
  private final PhasePaymentRepository phasePaymentRepository;
  private final PhasePaymentStatusLogRepository phasePaymentStatusLogRepository;
  private final PhaseDisbursementRepository phaseDisbursementRepository;
  private final PhaseDisbursementStatusLogRepository phaseDisbursementStatusLogRepository;
  private final TokenPurchaseRepository tokenPurchaseRepository;
  private final TokenPurchaseStatusLogRepository tokenPurchaseStatusLogRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionStatusLogRepository subscriptionStatusLogRepository;
  private final UserRepository userRepository;

  /** Resolves an actor id to a lazy reference without issuing a query. Null-safe. */
  public User actorRef(Long userId) {
    return userId == null ? null : userRepository.getReferenceById(userId);
  }

  @Transactional
  public Project transitionProject(
      Project project,
      ProjectStatus to,
      User actor,
      ActorType actorType,
      String action,
      Map<String, Object> metadata) {
    String from = project.getStatus() == null ? null : project.getStatus().name();
    project.setStatus(to);
    Project saved = projectRepository.save(project);
    ProjectStatusLog entry = new ProjectStatusLog();
    entry.setProject(saved);
    projectStatusLogRepository.save(
        fill(entry, from, to.name(), actor, actorType, action, metadata));
    return saved;
  }

  @Transactional
  public PhasePayment transitionPhasePayment(
      PhasePayment payment,
      PhasePaymentStatus to,
      User actor,
      ActorType actorType,
      String action,
      Map<String, Object> metadata) {
    String from = payment.getStatus() == null ? null : payment.getStatus().name();
    payment.setStatus(to);
    PhasePayment saved = phasePaymentRepository.save(payment);
    PhasePaymentStatusLog entry = new PhasePaymentStatusLog();
    entry.setPhasePayment(saved);
    phasePaymentStatusLogRepository.save(
        fill(entry, from, to.name(), actor, actorType, action, metadata));
    return saved;
  }

  @Transactional
  public PhaseDisbursement transitionDisbursement(
      PhaseDisbursement disbursement,
      DisbursementStatus to,
      User actor,
      ActorType actorType,
      String action,
      Map<String, Object> metadata) {
    String from = disbursement.getStatus() == null ? null : disbursement.getStatus().name();
    disbursement.setStatus(to);
    PhaseDisbursement saved = phaseDisbursementRepository.save(disbursement);
    PhaseDisbursementStatusLog entry = new PhaseDisbursementStatusLog();
    entry.setDisbursement(saved);
    phaseDisbursementStatusLogRepository.save(
        fill(entry, from, to.name(), actor, actorType, action, metadata));
    return saved;
  }

  @Transactional
  public TokenPurchase transitionTokenPurchase(
      TokenPurchase purchase,
      PurchaseStatus to,
      User actor,
      ActorType actorType,
      String action,
      Map<String, Object> metadata) {
    String from = purchase.getStatus() == null ? null : purchase.getStatus().name();
    purchase.setStatus(to);
    TokenPurchase saved = tokenPurchaseRepository.save(purchase);
    TokenPurchaseStatusLog entry = new TokenPurchaseStatusLog();
    entry.setTokenPurchase(saved);
    tokenPurchaseStatusLogRepository.save(
        fill(entry, from, to.name(), actor, actorType, action, metadata));
    return saved;
  }

  @Transactional
  public Subscription transitionSubscription(
      Subscription subscription,
      SubscriptionStatus to,
      User actor,
      ActorType actorType,
      String action,
      Map<String, Object> metadata) {
    String from = subscription.getStatus() == null ? null : subscription.getStatus().name();
    subscription.setStatus(to);
    Subscription saved = subscriptionRepository.save(subscription);
    SubscriptionStatusLog entry = new SubscriptionStatusLog();
    entry.setSubscription(saved);
    subscriptionStatusLogRepository.save(
        fill(entry, from, to.name(), actor, actorType, action, metadata));
    return saved;
  }

  private <T extends StatusLogEntry> T fill(
      T entry,
      String from,
      String to,
      User actor,
      ActorType actorType,
      String action,
      Map<String, Object> metadata) {
    entry.setFromStatus(from);
    entry.setToStatus(to);
    entry.setActor(actor);
    entry.setActorType(actorType);
    entry.setAction(action);
    entry.setMetadata(metadata);
    return entry;
  }
}
