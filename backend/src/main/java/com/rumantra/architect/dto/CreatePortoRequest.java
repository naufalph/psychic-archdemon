package com.rumantra.architect.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePortoRequest {

  @NotBlank(message = "Title is required")
  private String title;

  private String description;

  @NotNull(message = "Project date is required")
  private LocalDate projectDate;

  private String location;

  private String projectType;

  @NotNull(message = "isBuilt field is required")
  private Boolean isBuilt;
}
