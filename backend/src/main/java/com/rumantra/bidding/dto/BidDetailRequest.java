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
public class BidDetailRequest {

  private String conceptStatement;
  private List<BidPaymentPhaseRequest> phases;
}
