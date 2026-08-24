package com.rumantra.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationDisputeResponse {

  private Long projectId;
  private String projectTitle;
  private String clientName;
  private String clientEmail;
  private String architectName;
  private String architectCompany;
  private BigDecimal bidAmount;
  private LocalDateTime acceptedAt;
  private LocalDateTime expiredAt;
}
