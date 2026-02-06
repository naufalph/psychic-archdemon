package com.rumantra.architect.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArchitectProfileRequest {
  private String companyName;
  private String city;
  private String experienceRange;
  private String philosophy;
  private List<String> expertise;
}
