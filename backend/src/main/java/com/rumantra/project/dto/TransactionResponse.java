package com.rumantra.project.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One money movement, read from the append-only status ledger. {@code direction} is IN for client
 * payments into escrow and OUT for disbursements to the architect.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

  private LocalDateTime at;
  private String action;
  private String actorType;
  private String direction;
  private BigDecimal amount;
  private String reference;
  private Integer phaseNumber;
  private String phaseTitle;
}
