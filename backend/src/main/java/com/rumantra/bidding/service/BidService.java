package com.rumantra.bidding.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.rumantra.bidding.event.BidAcceptedEvent;
import com.rumantra.bidding.event.BidSubmittedEvent;
import com.rumantra.bidding.repository.BidPortfolioRefRepository;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.chat.domain.Conversation;
import com.rumantra.chat.repository.ConversationRepository;
import com.rumantra.chat.service.ConversationService;
import com.rumantra.client.domain.Project;
import com.rumantra.client.domain.ProjectFile;
import com.rumantra.client.domain.ProjectStatus;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;

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

  @Autowired private ConversationRepository conversationRepository;

  @Autowired private ConversationService conversationService;

  @Autowired private ApplicationEventPublisher eventPublisher;

  @Transactional
  public BidResponse createDraftBid(CreateBidRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.ARCHITECT_NOT_FOUND));

    Project project =
        projectRepository
            .findById(request.getProjectId())
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_NOT_FOUND));

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
            .proposal(request.getProposal())
            .status(BidStatus.DRAFT)
            .build();

    bid = bidRepository.save(bid);

    return mapToResponse(bid);
  }

  @Transactional
  public BidResponse submitBid(Long bidId) {
    Long userId = SecurityUtils.getCurrentUserId();
    Bid bid =
        bidRepository
            .findById(bidId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.BID_NOT_FOUND));

    if (!bid.getArchitect().getUser().getId().equals(userId)) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_BID_ACCESS);
    }

    if (bid.getStatus() != BidStatus.DRAFT) {
      throw new RuntimeException("Can only submit draft bids. Current status: " + bid.getStatus());
    }

    validateArchitectIdentity(bid.getArchitect());
    bidDetailService.validateForSubmission(bid);

    bidQuotaService.consumeToken(bid.getArchitect().getId());

    bid.setStatus(BidStatus.PENDING);
    bid.setSubmittedAt(LocalDateTime.now());
    bid = bidRepository.save(bid);

    BidQuota quota = bidQuotaService.getQuotaByArchitectId(bid.getArchitect().getId());
    bidUsageLogService.logBidPlaced(bid.getArchitect(), bid, quota.getTokensRemaining());

    Project project = bid.getProject();
    com.rumantra.client.domain.Client client = project.getClient();
    com.rumantra.user.domain.User clientUser = client.getUser();
    com.rumantra.user.domain.User architectUser = bid.getArchitect().getUser();

    String architectName;
    if (architectUser.getFirstName() != null && architectUser.getLastName() != null) {
      architectName = architectUser.getFirstName() + " " + architectUser.getLastName();
    } else {
      architectName = architectUser.getEmail().split("@")[0];
    }

    eventPublisher.publishEvent(
        new BidSubmittedEvent(
            this,
            bid.getId(),
            project.getId(),
            bid.getArchitect().getId(),
            client.getId(),
            clientUser.getId(),
            project.getTitle(),
            architectName));

    return mapToResponse(bid);
  }

  @Transactional
  public BidResponse updateDraftBid(Long bidId, UpdateBidRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Bid bid =
        bidRepository
            .findById(bidId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.BID_NOT_FOUND));

    if (!bid.getArchitect().getUser().getId().equals(userId)) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_BID_ACCESS);
    }

    if (bid.getStatus() != BidStatus.DRAFT) {
      throw new BusinessException(ExceptionConstants.BID_NOT_DRAFT);
    }

    if (request.getBidAmount() != null) {
      bid.setBidAmount(request.getBidAmount());
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
    Bid bid =
        bidRepository
            .findById(bidId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.BID_NOT_FOUND));

    if (!bid.getArchitect().getUser().getId().equals(userId)) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_BID_ACCESS);
    }

    if (bid.getStatus() != BidStatus.DRAFT) {
      throw new BusinessException(ExceptionConstants.BID_NOT_DRAFT);
    }

    bidDetailService.createOrUpdate(bid, request);
    return mapToResponse(bid);
  }

  @Transactional
  public BidResponse linkPortfolios(Long bidId, LinkPortfoliosRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Bid bid =
        bidRepository
            .findById(bidId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.BID_NOT_FOUND));

    if (!bid.getArchitect().getUser().getId().equals(userId)) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_BID_ACCESS);
    }

    if (bid.getStatus() != BidStatus.DRAFT) {
      throw new BusinessException(ExceptionConstants.BID_NOT_DRAFT);
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
          BidPortfolioRef.builder().bid(bid).porto(porto).displayOrder(displayOrder++).build();

      bidPortfolioRefRepository.save(ref);
    }

    return mapToResponse(bid);
  }

  @Transactional(readOnly = true)
  public List<BidResponse> getBidsByArchitect(Long architectId) {
    List<Bid> bids = bidRepository.findByArchitectId(architectId);
    return bids.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<BidResponse> getBidsByProject(Long projectId) {
    List<Bid> bids = bidRepository.findByProjectIdAndStatusNot(projectId, BidStatus.DRAFT);
    return bids.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public BidResponse getBidById(Long bidId) {
    Bid bid =
        bidRepository
            .findById(bidId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.BID_NOT_FOUND));

    Long userId = SecurityUtils.getCurrentUserId();
    boolean isArchitect = bid.getArchitect().getUser().getId().equals(userId);
    boolean isProjectClient = bid.getProject().getClient().getUser().getId().equals(userId);
    if (!isArchitect && !isProjectClient) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_BID_ACCESS);
    }

    return mapToResponse(bid);
  }

  public Bid getBidEntityById(Long bidId, Long architectId) {
    Bid bid =
        bidRepository
            .findById(bidId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.BID_NOT_FOUND));

    if (!bid.getArchitect().getId().equals(architectId)) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_BID_ACCESS);
    }

    return bid;
  }

  @Transactional
  public void withdrawBid(Long bidId) {
    Long userId = SecurityUtils.getCurrentUserId();
    Bid bid =
        bidRepository
            .findById(bidId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.BID_NOT_FOUND));

    if (!bid.getArchitect().getUser().getId().equals(userId)) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_BID_ACCESS);
    }

    if (bid.getStatus() != BidStatus.DRAFT
        && bid.getStatus() != BidStatus.PENDING
        && bid.getStatus() != BidStatus.ACCEPTED) {
      throw new RuntimeException(
          "Can only withdraw draft, pending, or accepted bids. Current status: " + bid.getStatus());
    }

    bid.setStatus(BidStatus.WITHDRAWN);
    bidRepository.save(bid);
  }

  @Transactional
  public void refundBid(Bid bid, String reason) {
    bid.setStatus(BidStatus.REFUNDED);
    bidRepository.save(bid);

    bidQuotaService.refundToken(bid.getArchitect().getId());

    BidQuota quota = bidQuotaService.getQuotaByArchitectId(bid.getArchitect().getId());
    bidUsageLogService.logBidRefunded(bid.getArchitect(), bid, reason, quota.getTokensRemaining());
  }

  private BidResponse mapToResponse(Bid bid) {
    Long conversationId =
        conversationRepository
            .findProjectConversationByBidId(bid.getId())
            .map(Conversation::getId)
            .orElse(null);

    return BidResponse.builder()
        .id(bid.getId())
        .projectId(bid.getProject().getId())
        .projectTitle(bid.getProject().getTitle())
        .projectLocation(bid.getProject().getLocation())
        .projectStatus(
            bid.getProject().getStatus() != null ? bid.getProject().getStatus().name() : null)
        .projectCoverImagePath(
            bid.getProject().getFiles().stream()
                .filter(
                    f ->
                        f.getFileType() != null
                            && (f.getFileType().equals("png")
                                || f.getFileType().equals("jpg")
                                || f.getFileType().equals("jpeg")
                                || f.getFileType().equals("webp")))
                .findFirst()
                .map(ProjectFile::getFilePath)
                .orElse(null))
        .architectId(bid.getArchitect().getId())
        .architectName(
            bid.getArchitect().getUser().getFirstName()
                + " "
                + bid.getArchitect().getUser().getLastName())
        .architectCompany(bid.getArchitect().getCompanyName())
        .architectCity(bid.getArchitect().getCity())
        .architectEducation(bid.getArchitect().getEducation())
        .bidAmount(bid.getBidAmount())
        .proposedTimelineDays(bid.getProposedTimelineDays())
        .proposal(bid.getProposal())
        .status(bid.getStatus())
        .createdAt(bid.getCreatedAt())
        .updatedAt(bid.getUpdatedAt())
        .submittedAt(bid.getSubmittedAt())
        .acceptedAt(bid.getAcceptedAt())
        .details(bidDetailService.getDetailResponse(bid.getId()))
        .facadeImages(bidImageService.getImagesByType(bid.getId(), BidImageType.FACADE))
        .interiorImages(bidImageService.getImagesByType(bid.getId(), BidImageType.INTERIOR))
        .massingImages(bidImageService.getImagesByType(bid.getId(), BidImageType.MASSING))
        .zoningImages(bidImageService.getImagesByType(bid.getId(), BidImageType.ZONING))
        .portfolioReferences(getPortfolioReferences(bid.getId()))
        .conversationId(conversationId)
        .build();
  }

  private List<PortoListResponse> getPortfolioReferences(Long bidId) {
    List<BidPortfolioRef> refs = bidPortfolioRefRepository.findByBidIdOrderByDisplayOrder(bidId);

    return refs.stream()
        .map(
            ref -> {
              Porto porto = ref.getPorto();
              List<PortoDetailResponse> images =
                  porto.getDetails().stream()
                      .sorted(Comparator.comparingInt(PortoDetail::getDisplayOrder))
                      .map(this::mapToPortoDetailResponse)
                      .collect(Collectors.toList());

              return PortoListResponse.builder()
                  .id(porto.getId())
                  .architectId(porto.getArchitect().getId())
                  .title(porto.getTitle())
                  .description(porto.getDescription())
                  .projectDate(porto.getProjectDate())
                  .location(porto.getLocation())
                  .projectType(porto.getProjectType())
                  .isBuilt(porto.isBuilt())
                  .images(images)
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

  @Transactional
  public BidResponse acceptBid(Long bidId) {
    Long userId = SecurityUtils.getCurrentUserId();

    Bid bid =
        bidRepository
            .findById(bidId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.BID_NOT_FOUND));

    Project project = bid.getProject();

    if (!project.getClient().getUser().getId().equals(userId)) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_PROJECT_ACCESS);
    }

    if (bid.getStatus() != BidStatus.PENDING) {
      throw new RuntimeException(
          "Only pending bids can be accepted. Current status: " + bid.getStatus());
    }

    if (project.getStatus() != ProjectStatus.OPEN) {
      throw new RuntimeException("Project is not open for bidding");
    }

    bid.setStatus(BidStatus.ACCEPTED);
    bid.setAcceptedAt(LocalDateTime.now());
    bid = bidRepository.save(bid);

    project.setStatus(ProjectStatus.NEGOTIATION);
    projectRepository.save(project);

    conversationService.createConversation(
        bid.getId(), project.getId(), bid.getArchitect().getId(), project.getClient().getId());

    List<Bid> siblingBids =
        bidRepository.findPendingBidsWithArchitect(project.getId(), BidStatus.PENDING);
    List<BidAcceptedEvent.RejectedArchitect> rejectedArchitects = new java.util.ArrayList<>();
    for (Bid sibling : siblingBids) {
      if (sibling.getId().equals(bid.getId())) continue;
      sibling.setStatus(BidStatus.REJECTED);
      bidRepository.save(sibling);
      com.rumantra.user.domain.User siblingUser = sibling.getArchitect().getUser();
      rejectedArchitects.add(
          new BidAcceptedEvent.RejectedArchitect(
              siblingUser.getId(), siblingUser.getEmail(), resolveDisplayName(siblingUser)));
    }

    com.rumantra.user.domain.User winningArchitectUser = bid.getArchitect().getUser();
    eventPublisher.publishEvent(
        new BidAcceptedEvent(
            this,
            bid.getId(),
            project.getId(),
            project.getTitle(),
            winningArchitectUser.getId(),
            winningArchitectUser.getEmail(),
            resolveDisplayName(winningArchitectUser),
            rejectedArchitects));

    return mapToResponse(bid);
  }

  private String resolveDisplayName(com.rumantra.user.domain.User user) {
    if (user.getFirstName() != null && user.getLastName() != null) {
      return user.getFirstName() + " " + user.getLastName();
    }
    return user.getEmail().split("@")[0];
  }

  private void validateArchitectIdentity(Architect architect) {
    boolean missingKtp = architect.getKtpNum() == null || architect.getKtpNum().isBlank();
    boolean missingNpwp = architect.getNpwp() == null || architect.getNpwp().isBlank();
    boolean missingFullname =
        architect.getFullnameKtp() == null || architect.getFullnameKtp().isBlank();
    boolean missingPhone =
        architect.getPhoneNumber() == null || architect.getPhoneNumber().isBlank();

    if (missingKtp || missingNpwp || missingFullname || missingPhone) {
      throw new IllegalStateException(
          "IDENTITY_INCOMPLETE: Lengkapi informasi identitas (KTP, NPWP, nama lengkap sesuai KTP, dan nomor HP) sebelum mengajukan penawaran.");
    }
  }
}
