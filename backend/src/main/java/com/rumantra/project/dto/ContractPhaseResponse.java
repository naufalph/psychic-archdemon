package com.rumantra.project.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractPhaseResponse {

  private Long phaseId;
  private Integer phaseNumber;
  private String title;
  private BigDecimal amount;

  /** Percentage of the contract total, rounded to one decimal place. */
  private BigDecimal share;

  private LocalDate dueDate;
  private String status;
  private String paymentStatus;
}
