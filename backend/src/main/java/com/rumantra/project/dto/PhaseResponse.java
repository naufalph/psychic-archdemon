package com.rumantra.project.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhaseResponse {

  private Long id;
  private Long projectId;
  private Integer phaseNumber;
  private String title;
  private String description;
  private BigDecimal amount;
  private String status;
  private LocalDate dueDate;
  private String paymentStatus;
  private String paymentLink;
  private Integer maxRevisions;
  private Integer revisionsUsed;
  private String disbursementStatus;
  private List<DeliverableResponse> deliverables;
  private List<DeliverableItemResponse> deliverableItems;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
