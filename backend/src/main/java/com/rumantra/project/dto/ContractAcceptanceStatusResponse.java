package com.rumantra.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The cheap read the finalization page polls: has this caller signed, and has the other side.
 * Deliberately excludes the contract body, which is large and only needed when the modal opens.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractAcceptanceStatusResponse {

  private Long contractId;
  private String contentHash;
  private String myParty;
  private String expectedSignatureName;
  private boolean clientAccepted;
  private boolean architectAccepted;
  private ContractAcceptanceResponse myAcceptance;
  private ContractAcceptanceResponse clientAcceptance;
  private ContractAcceptanceResponse architectAcceptance;
}
