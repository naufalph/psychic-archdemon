package com.rumantra.bidding.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.architect.domain.Architect;
import com.rumantra.bidding.domain.BidQuota;
import com.rumantra.bidding.domain.ResetInterval;
import com.rumantra.bidding.dto.BidQuotaResponse;
import com.rumantra.bidding.repository.BidQuotaRepository;
import com.rumantra.subscription.domain.SubscriptionTier;

@Service
public class BidQuotaService {

  @Autowired private BidQuotaRepository bidQuotaRepository;

  @Transactional
  public BidQuota initializeBidQuota(Architect architect) {
    // Check if quota already exists
    if (bidQuotaRepository.findByArchitectId(architect.getId()).isPresent()) {
      throw new RuntimeException("Bid quota already initialized for this architect");
    }

    LocalDateTime now = LocalDateTime.now();
    BidQuota quota =
        BidQuota.builder()
            .architect(architect)
            .tier(SubscriptionTier.FREE)
            .totalBidsAllowed(3)
            .bidsUsed(0)
            .resetInterval(ResetInterval.BI_WEEKLY)
            .lastResetDate(now)
            .nextResetDate(now.plusDays(14)) // BI_WEEKLY = 14 days
            .build();

    return bidQuotaRepository.save(quota);
  }

  public BidQuota getQuotaByArchitectId(Long architectId) {
    return bidQuotaRepository
        .findByArchitectId(architectId)
        .orElseThrow(
            () -> new RuntimeException("Bid quota not found. Please activate architect role."));
  }

  public BidQuotaResponse getQuotaResponse(Long architectId) {
    BidQuota quota = getQuotaByArchitectId(architectId);

    return BidQuotaResponse.builder()
        .id(quota.getId())
        .tier(quota.getTier())
        .totalBidsAllowed(quota.getTotalBidsAllowed())
        .bidsUsed(quota.getBidsUsed())
        .bidsRemaining(quota.getBidsRemaining())
        .resetInterval(quota.getResetInterval())
        .lastResetDate(quota.getLastResetDate())
        .nextResetDate(quota.getNextResetDate())
        .build();
  }

  @Transactional
  public void deductQuota(Long architectId) {
    BidQuota quota = getQuotaByArchitectId(architectId);

    if (quota.getBidsRemaining() <= 0) {
      throw new RuntimeException("No bids remaining. Next reset: " + quota.getNextResetDate());
    }

    quota.setBidsUsed(quota.getBidsUsed() + 1);
    bidQuotaRepository.save(quota);
  }

  @Transactional
  public void refundQuota(Long architectId) {
    BidQuota quota = getQuotaByArchitectId(architectId);

    if (quota.getBidsUsed() > 0) {
      quota.setBidsUsed(quota.getBidsUsed() - 1);
      bidQuotaRepository.save(quota);
    }
  }

  @Transactional
  public void resetQuota(BidQuota quota) {
    LocalDateTime now = LocalDateTime.now();
    quota.setBidsUsed(0);
    quota.setLastResetDate(now);

    // Calculate next reset based on interval
    if (quota.getResetInterval() == ResetInterval.BI_WEEKLY) {
      quota.setNextResetDate(now.plusDays(14));
    } else if (quota.getResetInterval() == ResetInterval.MONTHLY) {
      quota.setNextResetDate(now.plusMonths(1));
    }

    bidQuotaRepository.save(quota);
  }

  @Transactional
  public void upgradeToPremium(Long architectId) {
    BidQuota quota = getQuotaByArchitectId(architectId);

    quota.setTier(SubscriptionTier.PREMIUM);
    quota.setTotalBidsAllowed(18);
    quota.setResetInterval(ResetInterval.MONTHLY);
    quota.setBidsUsed(0); // Bonus reset on upgrade
    quota.setLastResetDate(LocalDateTime.now());
    quota.setNextResetDate(LocalDateTime.now().plusMonths(1));

    bidQuotaRepository.save(quota);
  }

  @Transactional
  public void downgradeToFree(Long architectId) {
    BidQuota quota = getQuotaByArchitectId(architectId);

    quota.setTier(SubscriptionTier.FREE);
    quota.setTotalBidsAllowed(3);
    quota.setResetInterval(ResetInterval.BI_WEEKLY);
    // Keep current bidsUsed, but cap at new limit
    if (quota.getBidsUsed() > 3) {
      quota.setBidsUsed(3);
    }
    quota.setLastResetDate(LocalDateTime.now());
    quota.setNextResetDate(LocalDateTime.now().plusDays(14));

    bidQuotaRepository.save(quota);
  }
}
