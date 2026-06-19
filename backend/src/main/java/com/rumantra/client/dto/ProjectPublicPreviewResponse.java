package com.rumantra.client.dto;

import com.rumantra.client.domain.ProjectStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPublicPreviewResponse {

  private Long id;
  private String title;
  private String location;
  private Long budgetDisplay;
  private String projectCategory;
  private String buildingFunction;
  private ProjectStatus status;
  private String firstImageUrl;
}
