package com.rumantra.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenPurchaseDetailResponse {
  private Long id;
  private Integer quantity;
  private BigDecimal pricePerToken;
  private BigDecimal totalAmount;
  private String tier;
  private String status;
  private String paymentMethod;
  private String paymentChannel;
  private LocalDateTime createdAt;
  private LocalDateTime completedAt;
  private LocalDateTime expiresAt;
  private String failureReason;
}
