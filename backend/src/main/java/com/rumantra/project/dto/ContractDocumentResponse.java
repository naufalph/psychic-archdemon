package com.rumantra.project.dto;

import java.sql.Timestamp;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The signed agreement itself: both renderings, the terms they were built from, the signatures. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractDocumentResponse {

  private Long contractId;
  private Long projectId;
  private String templateVersion;
  private String contentHash;
  private Timestamp generatedAt;
  private String bodyEn;
  private String bodyId;
  private ContractTermsSnapshot terms;
  private List<ContractAcceptanceResponse> acceptances;
  private String myParty;
}
