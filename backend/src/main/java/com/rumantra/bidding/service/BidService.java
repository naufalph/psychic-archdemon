package com.rumantra.bidding.service;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.domain.Porto;
import com.rumantra.architect.domain.PortoDetail;
import com.rumantra.architect.dto.PortoDetailResponse;
import com.rumantra.architect.dto.PortoListResponse;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.architect.repository.PortoRepository;
import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidImageType;
import com.rumantra.bidding.domain.BidPortfolioRef;
import com.rumantra.bidding.domain.BidQuota;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.dto.BidDetailRequest;
import com.rumantra.bidding.dto.BidResponse;
import com.rumantra.bidding.dto.CreateBidRequest;
import com.rumantra.bidding.dto.LinkPortfoliosRequest;
import com.rumantra.bidding.dto.UpdateBidRequest;
import com.rumantra.bidding.repository.BidPortfolioRefRepository;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.Project;
import com.rumantra.client.domain.ProjectStatus;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BidService {

  @Autowired private BidRepository bidRepository;

  @Autowired private ProjectRepository projectRepository;

  @Autowired private ArchitectRepository architectRepository;

  @Autowired private PortoRepository portoRepository;

  @Autowired private BidQuotaService bidQuotaService;

  @Autowired private BidUsageLogService bidUsageLogService;

  @Autowired private BidDetailService bidDetailService;

  @Autowired private BidImageService bidImageService;

  @Autowired private BidPortfolioRefRepository bidPortfolioRefRepository;

  @Transactional
  public BidResponse createDraftBid(CreateBidRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Please activate architect role first"));

    Project project =
        projectRepository
            .findById(request.getProjectId())
            .orElseThrow(() -> new RuntimeException("Project not found"));

    if (project.getStatus() != ProjectStatus.OPEN) {
      throw new RuntimeException(
          "Project is not accepting bids. Current status: " + project.getStatus());
    }

    if (bidRepository.existsByProjectIdAndArchitectId(request.getProjectId(), architect.getId())) {
      throw new RuntimeException("You have already placed a bid on this project");
    }

    Bid bid =
        Bid.builder()
            .project(project)
            .architect(architect)
            .bidAmount(request.getBidAmount())
            .proposedTimelineDays(request.getProposedTimelineDays())
            .proposal(request.getProposal())
            .status(BidStatus.DRAFT)
            .build();

    bid = bidRepository.save(bid);

    return mapToResponse(bid);
  }

  @Transactional
  public BidResponse submitBid(Long bidId) {
    Long userId = SecurityUtils.getCurrentUserId();
    Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new RuntimeException("Bid not found"));

    if (!bid.getArchitect().getUser().getId().equals(userId)) {
      throw new RuntimeException("Access denied: not your bid");
    }

    if (bid.getStatus() != BidStatus.DRAFT) {
      throw new RuntimeException("Can only submit draft bids. Current status: " + bid.getStatus());
    }

    validateBidComplete(bidId);

    BidQuota quota = bidQuotaService.getQuotaByArchitectId(bid.getArchitect().getId());
    if (quota.getBidsRemaining() <= 0) {
      throw new RuntimeException(
          "No bids remaining. Upgrade to premium or wait for reset on " + quota.getNextResetDate());
    }

    bidQuotaService.deductQuota(bid.getArchitect().getId());

    bid.setStatus(BidStatus.PENDING);
    bid.setSubmittedAt(LocalDateTime.now());
    bid = bidRepository.save(bid);

    quota = bidQuotaService.getQuotaByArchitectId(bid.getArchitect().getId());
    bidUsageLogService.logBidPlaced(bid.getArchitect(), bid, quota.getBidsRemaining());

    return mapToResponse(bid);
  }

  @Transactional
  public BidResponse updateDraftBid(Long bidId, UpdateBidRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new RuntimeException("Bid not found"));

    if (!bid.getArchitect().getUser().getId().equals(userId)) {
      throw new RuntimeException("Access denied: not your bid");
    }

    if (bid.getStatus() != BidStatus.DRAFT) {
      throw new RuntimeException(
          "Can only update draft bids. Current status: " + bid.getStatus());
    }

    if (request.getBidAmount() != null) {
      bid.setBidAmount(request.getBidAmount());
    }
    if (request.getProposedTimelineDays() != null) {
      bid.setProposedTimelineDays(request.getProposedTimelineDays());
    }
    if (request.getProposal() != null) {
      bid.setProposal(request.getProposal());
    }

    bid = bidRepository.save(bid);
    return mapToResponse(bid);
  }

  @Transactional
  public BidResponse updateBidDetails(Long bidId, BidDetailRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new RuntimeException("Bid not found"));

    if (!bid.getArchitect().getUser().getId().equals(userId)) {
      throw new RuntimeException("Access denied: not your bid");
    }

    if (bid.getStatus() != BidStatus.DRAFT) {
      throw new RuntimeException(
          "Can only update details for draft bids. Current status: " + bid.getStatus());
    }

    bidDetailService.createOrUpdate(bid, request);
    return mapToResponse(bid);
  }

  @Transactional
  public BidResponse linkPortfolios(Long bidId, LinkPortfoliosRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new RuntimeException("Bid not found"));

    if (!bid.getArchitect().getUser().getId().equals(userId)) {
      throw new RuntimeException("Access denied: not your bid");
    }

    if (bid.getStatus() != BidStatus.DRAFT) {
      throw new RuntimeException(
          "Can only link portfolios to draft bids. Current status: " + bid.getStatus());
    }

    bidPortfolioRefRepository.deleteByBidId(bidId);

    int displayOrder = 0;
    for (Long portoId : request.getPortfolioIds()) {
      Porto porto =
          portoRepository
              .findById(portoId)
              .orElseThrow(() -> new RuntimeException("Portfolio not found: " + portoId));

      if (!porto.getArchitect().getId().equals(bid.getArchitect().getId())) {
        throw new RuntimeException("Portfolio " + portoId + " does not belong to you");
      }

      BidPortfolioRef ref =
          BidPortfolioRef.builder()
              .bid(bid)
              .porto(porto)
              .displayOrder(displayOrder++)
              .build();

      bidPortfolioRefRepository.save(ref);
    }

    return mapToResponse(bid);
  }

  private void validateBidComplete(Long bidId) {
    if (!bidDetailService.hasRequiredDetails(bidId)) {
      throw new RuntimeException("Concept statement is required before submission");
    }

    if (!bidImageService.hasMinimumConceptSketches(bidId)) {
      throw new RuntimeException("At least 1 concept sketch is required before submission");
    }
  }

  public List<BidResponse> getBidsByArchitect(Long architectId) {
    List<Bid> bids = bidRepository.findByArchitectId(architectId);
    return bids.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  public List<BidResponse> getBidsByProject(Long projectId) {
    List<Bid> bids = bidRepository.findByProjectId(projectId);
    return bids.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  public BidResponse getBidById(Long bidId) {
    Bid bid =
        bidRepository.findById(bidId).orElseThrow(() -> new RuntimeException("Bid not found"));

    Long userId = SecurityUtils.getCurrentUserId();
    if (!bid.getArchitect().getUser().getId().equals(userId)) {
      throw new RuntimeException("Access denied: not your bid");
    }

    return mapToResponse(bid);
  }

  public Bid getBidEntityById(Long bidId, Long architectId) {
    Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new RuntimeException("Bid not found"));

    if (!bid.getArchitect().getId().equals(architectId)) {
      throw new RuntimeException("Access denied: not your bid");
    }

    return bid;
  }

  @Transactional
  public void withdrawBid(Long bidId) {
    Long userId = SecurityUtils.getCurrentUserId();
    Bid bid =
        bidRepository.findById(bidId).orElseThrow(() -> new RuntimeException("Bid not found"));

    if (!bid.getArchitect().getUser().getId().equals(userId)) {
      throw new RuntimeException("Access denied: not your bid");
    }

    if (bid.getStatus() != BidStatus.DRAFT && bid.getStatus() != BidStatus.ACCEPTED) {
      throw new RuntimeException(
          "Can only withdraw draft or accepted bids. Current status: " + bid.getStatus());
    }

    bid.setStatus(BidStatus.WITHDRAWN);
    bidRepository.save(bid);
  }

  @Transactional
  public void refundBid(Bid bid, String reason) {
    bid.setStatus(BidStatus.REFUNDED);
    bidRepository.save(bid);

    // Refund quota
    bidQuotaService.refundQuota(bid.getArchitect().getId());

    // Log the refund
    BidQuota quota = bidQuotaService.getQuotaByArchitectId(bid.getArchitect().getId());
    bidUsageLogService.logBidRefunded(bid.getArchitect(), bid, reason, quota.getBidsRemaining());
  }

  @Transactional
  public void refundAllPendingBids(Long projectId, String reason) {
    List<Bid> pendingBids = bidRepository.findByProjectIdAndStatus(projectId, BidStatus.PENDING);

    for (Bid bid : pendingBids) {
      refundBid(bid, reason);
    }
  }

  private BidResponse mapToResponse(Bid bid) {
    return BidResponse.builder()
        .id(bid.getId())
        .projectId(bid.getProject().getId())
        .architectId(bid.getArchitect().getId())
        .architectName(
            bid.getArchitect().getUser().getFirstName()
                + " "
                + bid.getArchitect().getUser().getLastName())
        .architectCompany(bid.getArchitect().getCompanyName())
        .bidAmount(bid.getBidAmount())
        .proposedTimelineDays(bid.getProposedTimelineDays())
        .proposal(bid.getProposal())
        .status(bid.getStatus())
        .createdAt(bid.getCreatedAt())
        .updatedAt(bid.getUpdatedAt())
        .submittedAt(bid.getSubmittedAt())
        .acceptedAt(bid.getAcceptedAt())
        .rejectedAt(bid.getRejectedAt())
        .details(bidDetailService.getDetailResponse(bid.getId()))
        .conceptSketches(bidImageService.getImagesByType(bid.getId(), BidImageType.CONCEPT_SKETCH))
        .moodBoards(bidImageService.getImagesByType(bid.getId(), BidImageType.MOOD_BOARD))
        .portfolioReferences(getPortfolioReferences(bid.getId()))
        .build();
  }

  private List<PortoListResponse> getPortfolioReferences(Long bidId) {
    List<BidPortfolioRef> refs = bidPortfolioRefRepository.findByBidIdOrderByDisplayOrder(bidId);

    return refs.stream()
        .map(
            ref -> {
              Porto porto = ref.getPorto();
              PortoDetailResponse firstImage =
                  porto.getDetails().stream()
                      .min(Comparator.comparingInt(PortoDetail::getDisplayOrder))
                      .map(this::mapToPortoDetailResponse)
                      .orElse(null);

              return PortoListResponse.builder()
                  .id(porto.getId())
                  .architectId(porto.getArchitect().getId())
                  .title(porto.getTitle())
                  .description(porto.getDescription())
                  .projectDate(porto.getProjectDate())
                  .location(porto.getLocation())
                  .projectType(porto.getProjectType())
                  .isBuilt(porto.isBuilt())
                  .firstImage(firstImage)
                  .build();
            })
        .collect(Collectors.toList());
  }

  private PortoDetailResponse mapToPortoDetailResponse(PortoDetail detail) {
    return PortoDetailResponse.builder()
        .id(detail.getId())
        .originalUrl(detail.getOriginalUrl())
        .largeUrl(detail.getLargeUrl())
        .mediumUrl(detail.getMediumUrl())
        .displayOrder(detail.getDisplayOrder())
        .build();
  }
}
