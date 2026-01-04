package com.rumantra.chat.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageFileResponse {

  private Long id;
  private String fileName;
  private String fileUrl;
  private String fileType;
  private Long fileSize;
}
