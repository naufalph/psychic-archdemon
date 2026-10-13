package com.rumantra.landing.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sent by the survey on every step, signed in or not, so every field is optional but size-capped —
 * the create and progress endpoints accept unauthenticated writes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BriefRequest {

  @Size(max = 60, message = "Preset slug must not exceed 60 characters")
  private String presetSlug;

  @Size(max = 40, message = "Building function must not exceed 40 characters")
  private String buildingFunction;

  @Size(max = 20, message = "Project scope must not exceed 20 characters")
  private String projectScope;

  @Size(max = 60, message = "Sub-category must not exceed 60 characters")
  private String subCategory;

  @Size(max = 160, message = "Title must not exceed 160 characters")
  private String title;

  @Size(max = 20, message = "Survey step must not exceed 20 characters")
  private String furthestStep;
}
