package com.rumantra.landing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresetRequest {

  @NotBlank(message = "Slug is required")
  @Size(max = 60, message = "Slug must not exceed 60 characters")
  @Pattern(
      regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
      message = "Slug must be lowercase words separated by single hyphens")
  private String slug;

  @NotBlank(message = "English label is required")
  @Size(max = 120, message = "English label must not exceed 120 characters")
  private String labelEn;

  @NotBlank(message = "Indonesian label is required")
  @Size(max = 120, message = "Indonesian label must not exceed 120 characters")
  private String labelId;

  @Size(max = 60, message = "English eyebrow must not exceed 60 characters")
  private String eyebrowEn;

  @Size(max = 60, message = "Indonesian eyebrow must not exceed 60 characters")
  private String eyebrowId;

  @NotBlank(message = "Icon name is required")
  @Size(max = 40, message = "Icon name must not exceed 40 characters")
  private String iconName;

  @NotBlank(message = "Building function is required")
  @Size(max = 40, message = "Building function must not exceed 40 characters")
  private String buildingFunction;

  @NotBlank(message = "Project scope is required")
  @Size(max = 20, message = "Project scope must not exceed 20 characters")
  private String projectScope;

  @Size(max = 60, message = "Sub-category must not exceed 60 characters")
  private String subCategory;

  @Size(max = 160, message = "English default title must not exceed 160 characters")
  private String defaultTitleEn;

  @Size(max = 160, message = "Indonesian default title must not exceed 160 characters")
  private String defaultTitleId;

  @Min(value = 1, message = "Default lot size must be at least 1")
  private Integer defaultLotSize;

  @Min(value = 0, message = "Default design budget must not be negative")
  private Long defaultDesignBudget;

  @Size(max = 2000, message = "English default description must not exceed 2000 characters")
  private String defaultDescriptionEn;

  @Size(max = 2000, message = "Indonesian default description must not exceed 2000 characters")
  private String defaultDescriptionId;

  private Boolean active;
}
