package com.rumantra.bidding.dto;

import com.rumantra.bidding.domain.AttachmentFileType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidAttachmentResponse {
  private Long id;
  private AttachmentFileType fileType;
  private String fileUrl;
  private String fileName;
  private Long fileSize;
  private Integer displayOrder;
}
