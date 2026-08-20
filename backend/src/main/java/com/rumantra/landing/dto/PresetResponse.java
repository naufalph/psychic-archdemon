package com.rumantra.landing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PresetResponse {

  private Long id;
  private String slug;
  private String labelEn;
  private String labelId;
  private String eyebrowEn;
  private String eyebrowId;
  private String iconName;
  private String buildingFunction;
  private String projectScope;
  private String subCategory;
  private String defaultTitleEn;
  private String defaultTitleId;
  private Integer defaultLotSize;
  private Long defaultDesignBudget;
  private String defaultDescriptionEn;
  private String defaultDescriptionId;
  private int displayOrder;
  private boolean active;
}
