package com.rumantra.bidding.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidPaymentPhaseResponse {

  private Long id;
  private Integer phaseNumber;
  private String title;
  private List<String> deliverables;
  private BigDecimal amount;
  private Integer revisionRounds;
  private Integer estimatedDays;
  private Integer displayOrder;
}
