package com.rumantra.client.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

  private Long id;
  private Long clientId;
  private Long budgetMin;
  private Long budgetMax;
  private String projectCategory;
  private String buildingFunction;
  private Integer estimatedBuildArea;
  private Integer numberOfFloors;
  private Boolean ownsLand;
  private Boolean hasLegalDocuments;
  private String scopeOfWork;
  private List<String> deliverables;
  private String designPreferences;
  private String contactPerson;
  private LocalDate expectedStartDate;
  private List<ProjectFileDto> files;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
