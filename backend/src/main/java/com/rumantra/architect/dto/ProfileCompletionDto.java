package com.rumantra.architect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCompletionDto {
  private boolean basicInfoComplete;
  private boolean businessLocationComplete;
  private boolean identityDocsComplete;
  private boolean portfolioComplete;
  private int percent;
}
