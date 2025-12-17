package com.rumantra.bidding.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.architect.domain.Architect;
import com.rumantra.bidding.domain.BidQuota;
import com.rumantra.bidding.dto.BidQuotaResponse;
import com.rumantra.bidding.repository.BidQuotaRepository;
import com.rumantra.subscription.domain.SubscriptionTier;

@Service
public class BidQuotaService {

  @Autowired private BidQuotaRepository bidQuotaRepository;

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

    return BidQuotaResponse.builder()
        .id(quota.getId())
        .architectId(quota.getArchitect().getId())
        .tier(quota.getTier())
        .tokensRemaining(quota.getTokensRemaining())
        .tokensAllocated(quota.getTokensAllocated())
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
    BidQuota quota = getQuotaByArchitectId(architectId);

    if (quota.getTokensRemaining() <= 0) {
      throw new RuntimeException("No bid tokens remaining. Please upgrade to BASIC tier.");
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
