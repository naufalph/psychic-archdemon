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

  // Served unauthenticated to the landing page, so the site's exact whereabouts stay out:
  // no fullAddress, latitude or longitude here. City and province are coarse enough to be public.
  private String city;
  private String province;

  private Long budgetDisplay;
  private String projectCategory;
  private String buildingFunction;
  private String projectScope;
  private String subCategory;
  private ProjectStatus status;
  private String firstImageUrl;
}
