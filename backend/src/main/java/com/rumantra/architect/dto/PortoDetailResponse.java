package com.rumantra.architect.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortoDetailResponse {

  private Long id;
  private String originalUrl;
  private String largeUrl;
  private String mediumUrl;
  private int displayOrder;
}
