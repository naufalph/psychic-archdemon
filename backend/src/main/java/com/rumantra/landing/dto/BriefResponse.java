package com.rumantra.landing.dto;

import java.time.LocalDate;

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
  private String location;
  private Integer lotSize;
  private Long designBudgetTotal;
  private Long designBudgetMin;
  private Long designBudgetMax;
  private String description;
  private String phoneNumber;
  private String startDateType;
  private LocalDate expectedStartDate;
}
