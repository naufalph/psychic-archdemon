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

  @NotNull(message = "Client ID is required")
  private Long clientId;

  @NotNull(message = "Minimum budget is required")
  @Min(value = 0, message = "Minimum budget must be greater than or equal to 0")
  private Long budgetMin;

  @NotNull(message = "Maximum budget is required")
  @Min(value = 0, message = "Maximum budget must be greater than or equal to 0")
  private Long budgetMax;

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

  private LocalDate expectedStartDate;
}
