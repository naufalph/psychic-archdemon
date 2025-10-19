package com.rumantra.architect.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePortoRequest {

  private String title;
  private String description;
  private LocalDate projectDate;
  private String location;
  private String projectType;
  private Boolean isBuilt;
}
