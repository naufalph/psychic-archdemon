package com.rumantra.client.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.rumantra.client.domain.ProjectStatus;

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
  private String title;
  private String location;
  private Long budgetTotal;
  private Long designBudgetMin;
  private Long designBudgetMax;
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
  private ProjectStatus status;
  private Boolean isValid;
  private String validationNotes;
  private LocalDateTime biddingDeadline;
  private List<ProjectFileDto> files;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
