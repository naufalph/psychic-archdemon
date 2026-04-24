package com.rumantra.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhasePaymentResponse {
  private Long phaseId;
  private Integer phaseNumber;
  private String title;
  private List<String> deliverables;
  private BigDecimal amount;
  private String paymentStatus;
  private String paymentLink;
  private LocalDateTime paidAt;
}
