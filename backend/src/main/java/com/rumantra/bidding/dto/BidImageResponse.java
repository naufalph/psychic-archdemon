package com.rumantra.bidding.dto;

import com.rumantra.bidding.domain.BidImageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidImageResponse {

  private Long id;
  private BidImageType imageType;
  private String imageUrl;
  private Integer displayOrder;
  private String fileName;
  private Long fileSize;
  private boolean archived;
}
