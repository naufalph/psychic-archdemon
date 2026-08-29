package com.rumantra.bidding.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.rumantra.architect.dto.EducationEntry;
import com.rumantra.architect.dto.PortoListResponse;
import com.rumantra.bidding.domain.BidStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {

  private Long id;
  private Long projectId;
  private String projectTitle;
  private String projectLocation;
  private String projectStatus;
  private String projectCoverImagePath;
  private Long architectId;
  private String architectName;
  private String architectCompany;
  private String architectCity;
  private List<EducationEntry> architectEducation;
  private BigDecimal bidAmount;
  private Integer proposedTimelineDays;
  private String proposal;
  private BidStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime submittedAt;
  private LocalDateTime acceptedAt;
  private BidDetailResponse details;
  private List<BidImageResponse> facadeImages;
  private List<BidImageResponse> interiorImages;
  private List<BidImageResponse> massingImages;
  private List<BidImageResponse> zoningImages;
  private List<PortoListResponse> portfolioReferences;
  private Long conversationId;
}
