package com.rumantra.bidding.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidDetailResponse {

  private Long id;
  private String conceptStatement;
  private String projectRisks;
}
