package com.rumantra.project.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhaseLogResponse {

  private String actorType;
  private String action;
  private String fromStatus;
  private String toStatus;
  private Map<String, Object> metadata;
  private LocalDateTime createdAt;
}
