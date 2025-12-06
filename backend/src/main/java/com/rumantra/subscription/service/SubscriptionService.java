package com.rumantra.subscription.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.architect.domain.Architect;
import com.rumantra.bidding.service.BidQuotaService;
import com.rumantra.bidding.service.BidUsageLogService;
import com.rumantra.subscription.domain.PaymentStatus;
import com.rumantra.subscription.domain.Subscription;
import com.rumantra.subscription.domain.SubscriptionTier;
import com.rumantra.subscription.repository.SubscriptionRepository;

@Service
public class SubscriptionService {

  @Autowired private SubscriptionRepository subscriptionRepository;

  @Autowired private BidQuotaService bidQuotaService;

  @Autowired private BidUsageLogService bidUsageLogService;

  @Transactional
  public Subscription initializeFreeTier(Architect architect) {
    Subscription subscription =
        Subscription.builder()
            .architect(architect)
            .tier(SubscriptionTier.FREE)
            .startDate(LocalDate.now())
            .endDate(null) // FREE tier has no end date
            .paymentStatus(PaymentStatus.ACTIVE)
            .isActive(true)
            .build();

    return subscriptionRepository.save(subscription);
  }

  @Transactional
  public Subscription upgradeToPremium(
      Architect architect, String paymentMethodId, java.math.BigDecimal monthlyPrice) {
    // 1. Deactivate current subscription
    subscriptionRepository
        .findByArchitectIdAndIsActive(architect.getId(), true)
        .ifPresent(
            currentSub -> {
              currentSub.setIsActive(false);
              currentSub.setEndDate(LocalDate.now());
              subscriptionRepository.save(currentSub);
            });

    // 2. Create new premium subscription
    Subscription newSubscription =
        Subscription.builder()
            .architect(architect)
            .tier(SubscriptionTier.PREMIUM)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(1))
            .paymentStatus(PaymentStatus.ACTIVE)
            .monthlyPrice(monthlyPrice)
            .paymentMethodId(paymentMethodId)
            .isActive(true)
            .build();

    newSubscription = subscriptionRepository.save(newSubscription);

    // 3. Upgrade bid quota
    bidQuotaService.upgradeToPremium(architect.getId());

    // 4. Log the upgrade
    bidUsageLogService.logQuotaUpgraded(architect, 18); // Premium gets 18 bids

    return newSubscription;
  }

  @Transactional
  public Subscription downgradeToFree(Architect architect) {
    // 1. Deactivate current subscription
    subscriptionRepository
        .findByArchitectIdAndIsActive(architect.getId(), true)
        .ifPresent(
            currentSub -> {
              currentSub.setIsActive(false);
              currentSub.setEndDate(LocalDate.now());
              subscriptionRepository.save(currentSub);
            });

    // 2. Create new free subscription
    Subscription newSubscription =
        Subscription.builder()
            .architect(architect)
            .tier(SubscriptionTier.FREE)
            .startDate(LocalDate.now())
            .endDate(null)
            .paymentStatus(PaymentStatus.ACTIVE)
            .isActive(true)
            .build();

    newSubscription = subscriptionRepository.save(newSubscription);

    // 3. Downgrade bid quota
    bidQuotaService.downgradeToFree(architect.getId());

    // 4. Log the downgrade
    bidUsageLogService.logQuotaDowngraded(architect, 3); // Free gets 3 bids

    return newSubscription;
  }

  public Subscription getActiveSubscription(Long architectId) {
    return subscriptionRepository
        .findByArchitectIdAndIsActive(architectId, true)
        .orElseThrow(() -> new RuntimeException("No active subscription found"));
  }
}
