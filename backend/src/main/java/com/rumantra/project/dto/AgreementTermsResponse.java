package com.rumantra.project.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The terms that have real backing data. IP &amp; Drawings and Dispute Resolution are deliberately
 * absent: no field records them, so the frontend renders generic platform copy for those rather
 * than implying this architect agreed to specific terms.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgreementTermsResponse {

  private String scopeOfWork;
  private BigDecimal feeTotal;
  private Integer phaseCount;
  private Integer revisionsPerPhase;
  private Integer timelineDays;
}
