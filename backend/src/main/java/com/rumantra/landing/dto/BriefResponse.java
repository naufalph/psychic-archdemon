package com.rumantra.landing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BriefResponse {

  private String claimToken;
  private String buildingFunction;
  private String projectScope;
  private String subCategory;
  private String title;
  private String furthestStep;
}
