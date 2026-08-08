package com.rumantra.client.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {

  @Size(max = 255, message = "Title must not exceed 255 characters")
  private String title;

  @Size(max = 255, message = "Location must not exceed 255 characters")
  private String location;

  @Min(value = 0, message = "Budget total must be greater than or equal to 0")
  private Long budgetTotal;

  @Min(value = 0, message = "Design budget minimum must be greater than or equal to 0")
  private Long designBudgetMin;

  @Min(value = 0, message = "Design budget maximum must be greater than or equal to 0")
  private Long designBudgetMax;

  @Size(max = 255, message = "Project category must not exceed 255 characters")
  private String projectCategory;

  @Size(max = 255, message = "Building function must not exceed 255 characters")
  private String buildingFunction;

  @Min(value = 1, message = "Estimated build area must be at least 1 square meter")
  private Integer estimatedBuildArea;

  @Min(value = 1, message = "Number of floors must be at least 1")
  private Integer numberOfFloors;

  private Boolean ownsLand;

  private Boolean hasLegalDocuments;

  private String scopeOfWork;

  private List<String> deliverables;

  private String designPreferences;

  @Size(max = 255, message = "Contact person must not exceed 255 characters")
  private String contactPerson;

  private com.rumantra.client.domain.StartDateType startDateType;

  private LocalDate expectedStartDate;

  private LocalDate biddingDeadline;
}
