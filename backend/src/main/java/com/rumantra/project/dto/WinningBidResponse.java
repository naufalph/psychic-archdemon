package com.rumantra.project.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WinningBidResponse {

  private Long bidId;
  private Long architectId;
  private String architectName;
  private String companyName;
  private String city;
  private String photoUrl;
  private BigDecimal bidAmount;
  private Integer timelineDays;
  private Integer phaseCount;
  private Integer revisionsPerPhase;
}
