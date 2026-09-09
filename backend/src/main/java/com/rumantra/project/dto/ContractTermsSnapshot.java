package com.rumantra.project.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The accepted bid's terms, frozen at the moment the contract was generated. Serialised to jsonb on
 * the contract row and hashed to produce its content hash, so any change here changes the hash and
 * invalidates signatures that echoed the old one.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractTermsSnapshot {

  private Long projectId;
  private String projectTitle;
  private String projectCity;
  private String projectCategory;
  private String scopeOfWork;

  private Long clientUserId;
  private String clientName;

  private Long bidId;
  private Long architectId;
  private Long architectUserId;
  private String architectName;
  private String architectCompany;
  private String architectCity;

  private BigDecimal totalFee;
  private Integer timelineDays;
  private List<Phase> phases;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Phase {
    private Integer phaseNumber;
    private String title;
    private BigDecimal amount;
    private Integer estimatedDays;
    private Integer revisionRounds;
    private List<String> deliverables;
  }
}
