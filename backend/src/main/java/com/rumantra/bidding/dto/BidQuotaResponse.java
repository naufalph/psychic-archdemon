package com.rumantra.bidding.dto;

import java.time.LocalDateTime;

import com.rumantra.bidding.domain.ResetInterval;
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

  private Long id;
  private SubscriptionTier tier;
  private Integer totalBidsAllowed;
  private Integer bidsUsed;
  private Integer bidsRemaining;
  private ResetInterval resetInterval;
  private LocalDateTime lastResetDate;
  private LocalDateTime nextResetDate;
}
