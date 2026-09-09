package com.rumantra.project.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractAcceptanceResponse {

  private String party;
  private Long userId;
  private String signatureName;
  private String lang;
  private Timestamp acceptedAt;
}
