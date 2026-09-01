package com.rumantra.subscription.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.architect.domain.Architect;
import com.rumantra.bidding.service.BidQuotaService;
import com.rumantra.bidding.service.BidUsageLogService;
import com.rumantra.integration.xendit.XenditService;
import com.rumantra.integration.xendit.dto.XenditCreatePlanRequest;
import com.rumantra.integration.xendit.dto.XenditCreatePlanResponse;
import com.rumantra.integration.xendit.dto.XenditSchedule;
import com.rumantra.integration.xendit.dto.XenditWebhookEvent;
import com.rumantra.ledger.service.StatusTransitionService;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.domain.ActorType;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.subscription.domain.Subscription;
import com.rumantra.subscription.domain.SubscriptionStatus;
import com.rumantra.subscription.domain.SubscriptionTier;
import com.rumantra.subscription.repository.SubscriptionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SubscriptionService {

  @Autowired private SubscriptionRepository subscriptionRepository;
  @Autowired private StatusTransitionService statusTransitionService;

  @Autowired private BidQuotaService bidQuotaService;

  @Autowired private BidUsageLogService bidUsageLogService;

  @Autowired private XenditService xenditService;

  @Value("${app.base-url}")
  private String baseUrl;

  private static final BigDecimal BASIC_YEARLY_PRICE = new BigDecimal("1500000");
  private static final int FREE_TOKENS = 1;
  private static final int BASIC_TOKENS = 10;

  @Transactional
  public Subscription initializeFreeTier(Architect architect) {
    Subscription subscription =
        Subscription.builder()
            .architect(architect)
            .tier(SubscriptionTier.FREE)
            .startDate(LocalDate.now())
            .endDate(null)
            .status(SubscriptionStatus.ACTIVE)
            .yearlyPrice(BigDecimal.ZERO)
            .isActive(true)
            .build();

    subscription = subscriptionRepository.save(subscription);

    bidQuotaService.allocateTokens(architect.getId(), FREE_TOKENS);

    return subscription;
  }

  @Transactional
  public SubscriptionUpgradeResponse initiateUpgrade(Architect architect) {
    Subscription current = getActiveSubscription(architect.getId());

    if (current.getTier() == SubscriptionTier.BASIC) {
      throw new BusinessException(ExceptionConstants.ALREADY_BASIC_TIER);
    }

    String referenceId = "rumantra_arch_" + architect.getId() + "_" + System.currentTimeMillis();

    XenditCreatePlanRequest xenditRequest =
        XenditCreatePlanRequest.builder()
            .referenceId(referenceId)
            .customerId("arch_" + architect.getId())
            .recurringAction("PAYMENT")
            .currency("IDR")
            .amount(BASIC_YEARLY_PRICE)
            .schedule(
                XenditSchedule.builder()
                    .referenceId("schedule_" + System.currentTimeMillis())
                    .interval("YEAR")
                    .intervalCount(1)
                    .anchorDate(LocalDateTime.now().plusDays(1))
                    .retryInterval("DAY")
                    .retryIntervalCount(3)
                    .totalRetry(3)
                    .failedAttemptNotifications(List.of(1, 2, 3))
                    .build())
            .failedCycleAction("STOP")
            .successReturnUrl(baseUrl + "/subscription/success")
            .failureReturnUrl(baseUrl + "/subscription/failed")
            .build();

    XenditCreatePlanResponse xenditResponse = xenditService.createRecurringPlan(xenditRequest);

    Subscription newSubscription =
        Subscription.builder()
            .architect(architect)
            .tier(SubscriptionTier.BASIC)
            .status(SubscriptionStatus.PENDING)
            .xenditPlanId(xenditResponse.getId())
            .xenditReferenceId(referenceId)
            .paymentLink(
                xenditResponse.getActions() != null
                    ? xenditResponse.getActions().getDesktopWebCheckoutUrl()
                    : null)
            .yearlyPrice(BASIC_YEARLY_PRICE)
            .isActive(false)
            .build();

    subscriptionRepository.save(newSubscription);

    return SubscriptionUpgradeResponse.builder()
        .paymentLink(
            xenditResponse.getActions() != null
                ? xenditResponse.getActions().getDesktopWebCheckoutUrl()
                : null)
        .mobilePaymentLink(
            xenditResponse.getActions() != null
                ? xenditResponse.getActions().getMobileWebCheckoutUrl()
                : null)
        .status("PENDING")
        .build();
  }

  @Transactional
  public void handlePaymentSucceeded(XenditWebhookEvent event) {
    Subscription subscription =
        subscriptionRepository
            .findByXenditReferenceId(event.getReferenceId())
            .orElseThrow(() -> new RuntimeException("Subscription not found"));

    if (subscription.getXenditCycleId() != null
        && subscription.getXenditCycleId().equals(event.getRecurringCycleId())) {
      log.info("Duplicate webhook, already processed");
      return;
    }

    if (subscription.getStatus() == SubscriptionStatus.PENDING) {
      subscriptionRepository
          .findByArchitectIdAndIsActive(subscription.getArchitect().getId(), true)
          .ifPresent(
              oldSub -> {
                oldSub.setIsActive(false);
                oldSub.setEndDate(LocalDate.now());
                subscriptionRepository.save(oldSub);
              });

      statusTransitionService.transitionSubscription(
          subscription,
          SubscriptionStatus.ACTIVE,
          null,
          ActorType.XENDIT,
          "SUBSCRIPTION_ACTIVATED",
          null);
      subscription.setIsActive(true);
      subscription.setStartDate(LocalDate.now());
      subscription.setEndDate(LocalDate.now().plusYears(1));
      subscription.setNextBillingDate(LocalDate.now().plusYears(1));

      bidQuotaService.allocateTokens(subscription.getArchitect().getId(), BASIC_TOKENS);

      bidUsageLogService.logTokenAllocation(subscription.getArchitect(), BASIC_TOKENS);
    } else if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
      subscription.setEndDate(subscription.getEndDate().plusYears(1));
      subscription.setNextBillingDate(subscription.getNextBillingDate().plusYears(1));

      bidQuotaService.allocateTokens(subscription.getArchitect().getId(), BASIC_TOKENS);

      bidUsageLogService.logTokenAllocation(subscription.getArchitect(), BASIC_TOKENS);
    }

    subscription.setXenditCycleId(event.getRecurringCycleId());
    subscription.setLastPaymentDate(LocalDateTime.now());

    subscriptionRepository.save(subscription);
  }

  @Transactional
  public void handlePaymentFailed(XenditWebhookEvent event) {
    Subscription subscription =
        subscriptionRepository
            .findByXenditReferenceId(event.getReferenceId())
            .orElseThrow(() -> new RuntimeException("Subscription not found"));

    statusTransitionService.transitionSubscription(
        subscription,
        SubscriptionStatus.EXPIRED,
        null,
        ActorType.XENDIT,
        "SUBSCRIPTION_PAYMENT_FAILED",
        null);

    log.warn("Payment failed for subscription {}", subscription.getId());
  }

  @Transactional
  public void handleSubscriptionStopped(XenditWebhookEvent event) {
    Subscription subscription =
        subscriptionRepository
            .findByXenditReferenceId(event.getReferenceId())
            .orElseThrow(() -> new RuntimeException("Subscription not found"));

    subscription.setIsActive(false);
    statusTransitionService.transitionSubscription(
        subscription,
        SubscriptionStatus.CANCELLED,
        null,
        ActorType.XENDIT,
        "SUBSCRIPTION_CANCELLED",
        null);

    initializeFreeTier(subscription.getArchitect());

    log.info(
        "Subscription stopped and downgraded to FREE for architect {}",
        subscription.getArchitect().getId());
  }

  @Transactional
  public void cancelSubscription(Long architectId) {
    Subscription subscription = getActiveSubscription(architectId);

    if (subscription.getXenditPlanId() != null) {
      xenditService.stopRecurringPlan(subscription.getXenditPlanId());
    }

    statusTransitionService.transitionSubscription(
        subscription,
        SubscriptionStatus.CANCELLED,
        statusTransitionService.actorRef(SecurityUtils.getCurrentUserId()),
        ActorType.ARCHITECT,
        "SUBSCRIPTION_CANCELLED",
        null);
  }

  public Subscription getActiveSubscription(Long architectId) {
    return subscriptionRepository
        .findByArchitectIdAndIsActive(architectId, true)
        .orElseThrow(() -> new RuntimeException("No active subscription found"));
  }
}
