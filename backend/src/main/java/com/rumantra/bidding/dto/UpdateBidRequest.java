package com.rumantra.bidding.dto;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
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
