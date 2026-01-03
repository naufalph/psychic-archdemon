package com.rumantra.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenPurchaseResponse {
  private Long purchaseId;
  private Integer quantity;
  private BigDecimal pricePerToken;
  private BigDecimal totalAmount;
  private String paymentLink;
  private LocalDateTime expiresAt;
  private String status;
}
