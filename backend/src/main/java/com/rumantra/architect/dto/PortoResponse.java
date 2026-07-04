package com.rumantra.architect.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortoResponse {

  private Long id;
  private Long architectId;
  private String title;
  private String description;
  private LocalDate projectDate;
  private String location;
  private String projectType;
  private boolean isBuilt;
  private List<PortoDetailResponse> images;
  private boolean madeWithRumantra;
}
