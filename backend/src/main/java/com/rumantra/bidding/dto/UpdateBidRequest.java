package com.rumantra.bidding.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBidRequest {

  @Positive(message = "Bid amount must be positive")
  private BigDecimal bidAmount;

  @Positive(message = "Proposed timeline must be positive")
  private Integer proposedTimelineDays;

  private String proposal;
}
