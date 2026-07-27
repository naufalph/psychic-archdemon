package com.rumantra.legal.dto;

import java.sql.Timestamp;

import com.rumantra.legal.domain.LegalDocType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalDocumentResponseDto {
  private LegalDocType docType;
  private String lang;
  private String version;
  private String contentMd;
  private String contentHash;
  private Timestamp effectiveAt;
}
