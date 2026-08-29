package com.rumantra.architect.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationEntry implements Serializable {
  private String level; // "S1" | "S2" | "S3"
  private String universityName;
  private String fieldOfStudy;
  private Integer graduationYear;
}
