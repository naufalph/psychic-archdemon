package com.rumantra.project.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliverableResponse {

  private Long id;
  private String filePath;
  private String fileType;
  private String description;
  private Integer deliverableIndex;
  private Integer revisionRound;
  private LocalDateTime uploadedAt;
}
