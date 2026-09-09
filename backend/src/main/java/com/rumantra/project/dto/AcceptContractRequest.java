package com.rumantra.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcceptContractRequest {

  /** Echoed back from the document the signer was shown, to catch a stale open tab. */
  @NotBlank private String contentHash;

  @NotBlank private String signatureName;

  @NotBlank private String lang;
}
