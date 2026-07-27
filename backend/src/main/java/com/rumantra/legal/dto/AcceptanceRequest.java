package com.rumantra.legal.dto;

import com.rumantra.legal.domain.LegalDocType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptanceRequest {

  @NotNull(message = "Document type is required")
  private LegalDocType docType;

  @NotBlank(message = "Version is required")
  private String version;

  @NotBlank(message = "Content hash is required")
  private String contentHash;

  @NotBlank(message = "Language is required")
  private String lang;
}
