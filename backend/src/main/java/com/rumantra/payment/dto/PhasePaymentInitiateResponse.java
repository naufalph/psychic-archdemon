package com.rumantra.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhasePaymentInitiateResponse {
  private BigDecimal amount;
  private String paymentLink;
  private LocalDateTime expiresAt;
  private String status;
}
