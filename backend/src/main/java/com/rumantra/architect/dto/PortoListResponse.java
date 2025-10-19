package com.rumantra.architect.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Lightweight response for listing portfolios. Includes only the first image for card display. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortoListResponse {

  private Long id;
  private Long architectId;
  private String title;
  private String description;
  private LocalDate projectDate;
  private String location;
  private String projectType;
  private boolean isBuilt;
  private PortoDetailResponse firstImage; // First image for card thumbnail
}
