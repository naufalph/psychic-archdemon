package com.rumantra.bidding.service;

import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.architect.domain.Architect;
import com.rumantra.bidding.domain.BidQuota;
import com.rumantra.bidding.domain.BidUsageAction;
import com.rumantra.bidding.dto.BidQuotaResponse;
import com.rumantra.bidding.repository.BidQuotaRepository;
import com.rumantra.bidding.repository.BidUsageLogRepository;
import com.rumantra.subscription.domain.SubscriptionTier;

@Service
public class BidQuotaService {

  @Autowired private BidQuotaRepository bidQuotaRepository;
  @Autowired private BidUsageLogRepository bidUsageLogRepository;

  @Transactional
  public BidQuota initializeBidQuota(Architect architect) {
    if (bidQuotaRepository.findByArchitectId(architect.getId()).isPresent()) {
      throw new RuntimeException("Bid quota already initialized for this architect");
    }

    BidQuota quota =
        BidQuota.builder()
            .architect(architect)
            .tier(SubscriptionTier.FREE)
            .tokensAllocated(0)
            .tokensRemaining(0)
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

    YearMonth currentMonth = YearMonth.now();
    LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
    LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

    long usedThisMonth =
        bidUsageLogRepository.countByArchitectIdAndActionAndTimestampBetween(
            architectId, BidUsageAction.BID_PLACED, monthStart, monthEnd);

    return BidQuotaResponse.builder()
        .tier(quota.getTier())
        .tokensRemaining(quota.getTokensRemaining())
        .tokensUsedThisMonth((int) usedThisMonth)
        .build();
  }

  @Transactional
  public void allocateTokens(Long architectId, int tokens) {
    BidQuota quota = getQuotaByArchitectId(architectId);

    quota.setTokensRemaining(quota.getTokensRemaining() + tokens);
    quota.setTokensAllocated(quota.getTokensAllocated() + tokens);

    bidQuotaRepository.save(quota);
  }

  @Transactional
  public void consumeToken(Long architectId) {
    BidQuota quota =
        bidQuotaRepository
            .findByArchitectIdForUpdate(architectId)
            .orElseThrow(
                () -> new RuntimeException("Bid quota not found. Please activate architect role."));

    if (quota.getTokensRemaining() <= 0) {
      throw new RuntimeException(
          "No bid tokens remaining. Please upgrade to BASIC tier or purchase more tokens.");
    }

    quota.setTokensRemaining(quota.getTokensRemaining() - 1);
    bidQuotaRepository.save(quota);
  }

  @Transactional
  public void refundToken(Long architectId) {
    BidQuota quota = getQuotaByArchitectId(architectId);

    quota.setTokensRemaining(quota.getTokensRemaining() + 1);
    bidQuotaRepository.save(quota);
  }

  public int getRemainingTokens(Long architectId) {
    return getQuotaByArchitectId(architectId).getTokensRemaining();
  }
}
