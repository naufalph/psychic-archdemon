package com.rumantra.bidding.dto;

import com.rumantra.subscription.domain.SubscriptionTier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidQuotaResponse {

  private SubscriptionTier tier;
  private Integer tokensRemaining;
  private Integer tokensAllocated;
}
