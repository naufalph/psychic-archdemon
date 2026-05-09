package com.rumantra.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DisbursementResponse {

  private Long id;
  private Long phaseId;
  private String xenditPayoutId;
  private String xenditReferenceId;
  private String channelCode;
  private String accountNumber;
  private String accountHolderName;
  private BigDecimal amount;
  private String status;
  private String failureCode;
  private LocalDateTime initiatedAt;
  private LocalDateTime completedAt;
}
