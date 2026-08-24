package com.rumantra.bidding.dto;

import java.util.List;

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
  private List<BidPaymentPhaseResponse> phases;
  private String facadeDescription;
  private String interiorDescription;
  private String massingDescription;
  private String zoningDescription;
}
