package com.rumantra.project.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Everything the workspace's Contract &amp; Payment tab needs, in one read. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractResponse {

  private Long projectId;
  private BigDecimal totalValue;
  private BigDecimal paidValue;
  private BigDecimal disbursedValue;
  private List<ContractPhaseResponse> paymentSchedule;
  private AgreementTermsResponse agreementTerms;
  private WinningBidResponse winningBid;
  private List<TransactionResponse> transactions;
}
