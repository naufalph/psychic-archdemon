package com.rumantra.client.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFileDto {

  private Long id;
  private String fileName;
  private String filePath;
  private String fileType;
  private Long fileSize;
  private LocalDateTime uploadedAt;
}
