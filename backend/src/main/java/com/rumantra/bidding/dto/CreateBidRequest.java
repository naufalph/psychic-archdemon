package com.rumantra.bidding.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBidRequest {

  @NotNull(message = "Project ID is required")
  private Long projectId;

  @NotNull(message = "Bid amount is required")
  @Positive(message = "Bid amount must be positive")
  private BigDecimal bidAmount;

  private String proposal;
}
