package com.rumantra.architect.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.domain.Porto;
import com.rumantra.architect.domain.PortoDetail;
import com.rumantra.architect.dto.*;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.architect.repository.PortoDetailRepository;
import com.rumantra.architect.repository.PortoRepository;
import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.Project;
import com.rumantra.client.domain.ProjectStatus;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.project.domain.PhaseDeliverable;
import com.rumantra.project.domain.ProjectPhase;
import com.rumantra.project.repository.PhaseDeliverableRepository;
import com.rumantra.project.repository.ProjectPhaseRepository;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.storage.FileStorageService;
import com.rumantra.shared.storage.ImageSize;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortoService {

  private final PortoRepository portoRepository;
  private final PortoDetailRepository portoDetailRepository;
  private final ArchitectRepository architectRepository;
  private final FileStorageService fileStorageService;
  private final ProjectRepository projectRepository;
  private final BidRepository bidRepository;
  private final ProjectPhaseRepository projectPhaseRepository;
  private final PhaseDeliverableRepository phaseDeliverableRepository;

  @PersistenceContext private EntityManager entityManager;

  /**
   * Get the architect ID for the currently authenticated user.
   *
   * @return The architect ID
   * @throws RuntimeException if the user doesn't have an architect profile
   */
  private Long getCurrentUserArchitectId() {
    Long userId = SecurityUtils.getCurrentUserId();

    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Current user does not have an architect profile. Please activate the architect role first."));

    return architect.getId();
  }

  /**
   * Verify that the currently authenticated user owns the specified architect profile.
   *
   * @param architectId The architect ID to verify ownership for
   * @throws AccessDeniedException if the user is not the owner
   */
  private void verifyArchitectOwnership(Long architectId) {
    Long userId = SecurityUtils.getCurrentUserId();

    Architect architect =
        architectRepository
            .findById(architectId)
            .orElseThrow(() -> new RuntimeException("Architect not found with id: " + architectId));

    if (!architect.getUser().getId().equals(userId)) {
      log.warn(
          "Access denied: User {} attempted to access architect {} resources", userId, architectId);
      throw new AccessDeniedException(
          "You do not have permission to access this architect's resources");
    }

    log.debug("Ownership verified: User {} owns architect {}", userId, architectId);
  }

  /**
   * Verify that the currently authenticated user owns the portfolio.
   *
   * @param portoId The portfolio ID to verify ownership for
   * @throws AccessDeniedException if the user is not the owner
   */
  private void verifyPortoOwnership(Long portoId) {
    Porto porto =
        portoRepository
            .findById(portoId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portoId));

    verifyArchitectOwnership(porto.getArchitect().getId());
  }

  @Transactional
  public PortoResponse createPorto(CreatePortoRequest request, List<MultipartFile> images) {

    // Get current user's architect ID
    Long architectId = getCurrentUserArchitectId();

    // Get architect reference
    Architect architect = entityManager.getReference(Architect.class, architectId);

    // Create Porto entity
    Porto porto =
        Porto.builder()
            .architect(architect)
            .title(request.getTitle())
            .description(request.getDescription())
            .projectDate(request.getProjectDate())
            .location(request.getLocation())
            .projectType(request.getProjectType())
            .isBuilt(request.getIsBuilt())
            .build();

    // Save to get ID
    porto = portoRepository.save(porto);

    // Upload images if provided
    if (images != null && !images.isEmpty()) {
      addImagesToPorto(porto, images);
    }

    // Reload with details
    porto = portoRepository.findByIdWithDetails(porto.getId());

    return mapToPortoResponse(porto);
  }

  @Transactional
  public PortoResponse createPortoFromProject(Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();
    Long architectId = getCurrentUserArchitectId();

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

    if (project.getStatus() != ProjectStatus.COMPLETED) {
      throw new RuntimeException(
          "Project must be completed before it can be archived to portfolio");
    }

    List<Bid> acceptedBids = bidRepository.findByProjectIdAndStatus(projectId, BidStatus.ACCEPTED);
    boolean isWinningArchitect =
        acceptedBids.stream().anyMatch(b -> b.getArchitect().getUser().getId().equals(userId));
    if (!isWinningArchitect) {
      throw new AccessDeniedException("You are not the architect assigned to this project");
    }

    if (portoRepository.existsBySourceProjectId(projectId)) {
      throw new RuntimeException("This project has already been archived to a portfolio");
    }

    Architect architect = entityManager.getReference(Architect.class, architectId);

    Porto porto =
        Porto.builder()
            .architect(architect)
            .title(project.getTitle())
            .description(project.getScopeOfWork())
            .projectDate(LocalDate.now())
            .location(project.getLocation())
            .projectType(project.getProjectCategory())
            .isBuilt(true)
            .madeWithRumantra(true)
            .sourceProjectId(projectId)
            .build();

    porto = portoRepository.save(porto);

    addFinalDeliverableImages(porto, projectId);

    porto = portoRepository.findByIdWithDetails(porto.getId());

    log.info(
        "Architect {} archived project {} to portfolio {}", architectId, projectId, porto.getId());

    return mapToPortoResponse(porto);
  }

  private void addFinalDeliverableImages(Porto porto, Long projectId) {
    List<ProjectPhase> phases =
        projectPhaseRepository.findByProjectIdOrderByPhaseNumberAsc(projectId);

    int displayOrder = 0;
    for (ProjectPhase phase : phases) {
      List<PhaseDeliverable> deliverables =
          phaseDeliverableRepository.findByPhaseIdOrderByUploadedAtAsc(phase.getId());

      int latestRevision =
          deliverables.stream().mapToInt(PhaseDeliverable::getRevisionRound).max().orElse(-1);

      for (PhaseDeliverable deliverable : deliverables) {
        boolean isImage =
            deliverable.getFileType() != null && deliverable.getFileType().startsWith("image/");
        if (!isImage || deliverable.getRevisionRound() != latestRevision) {
          continue;
        }

        PortoDetail detail =
            PortoDetail.builder()
                .porto(porto)
                .originalUrl(deliverable.getFilePath())
                .largeUrl(deliverable.getFilePath())
                .mediumUrl(deliverable.getFilePath())
                .displayOrder(displayOrder++)
                .build();

        portoDetailRepository.save(detail);
      }
    }
  }

  public List<PortoListResponse> getPortosByArchitect() {
    // Get current user's architect ID
    Long architectId = getCurrentUserArchitectId();

    List<Porto> portos = portoRepository.findByArchitectIdWithDetails(architectId);

    return portos.stream()
        .map(
            porto -> {
              // Get all images sorted by display order
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
                  .madeWithRumantra(Boolean.TRUE.equals(porto.getMadeWithRumantra()))
                  .build();
            })
        .collect(Collectors.toList());
  }

  public PortoResponse getPortoById(Long portoId) {
    // Verify ownership before retrieving
    verifyPortoOwnership(portoId);

    Porto porto = portoRepository.findByIdWithDetails(portoId);

    if (porto == null) {
      throw new RuntimeException("Portfolio not found with id: " + portoId);
    }

    return mapToPortoResponse(porto);
  }

  @Transactional
  public PortoResponse updatePorto(Long portoId, UpdatePortoRequest request) {
    // Verify ownership before updating
    verifyPortoOwnership(portoId);

    Porto porto =
        portoRepository
            .findById(portoId)
            .orElseThrow(() -> new RuntimeException("Portfolio not found with id: " + portoId));

    // Title and description are locked for portfolios archived from a completed project
    boolean isLocked = Boolean.TRUE.equals(porto.getMadeWithRumantra());

    // Update fields if provided
    if (request.getTitle() != null && !isLocked) {
      porto.setTitle(request.getTitle());
    }
    if (request.getDescription() != null && !isLocked) {
      porto.setDescription(request.getDescription());
    }
    if (request.getProjectDate() != null) {
      porto.setProjectDate(request.getProjectDate());
    }
    if (request.getLocation() != null) {
      porto.setLocation(request.getLocation());
    }
    if (request.getProjectType() != null) {
      porto.setProjectType(request.getProjectType());
    }
    if (request.getIsBuilt() != null) {
      porto.setBuilt(request.getIsBuilt());
    }

    porto = portoRepository.save(porto);

    // Reload with details
    porto = portoRepository.findByIdWithDetails(porto.getId());

    return mapToPortoResponse(porto);
  }

  @Transactional
  public void deletePorto(Long portoId) {
    // Verify ownership before deleting
    verifyPortoOwnership(portoId);

    Porto porto = portoRepository.findByIdWithDetails(portoId);

    if (porto == null) {
      throw new RuntimeException("Portfolio not found with id: " + portoId);
    }

    // Collect all image URLs for deletion
    List<String> imageUrls = new ArrayList<>();
    for (PortoDetail detail : porto.getDetails()) {
      imageUrls.add(detail.getOriginalUrl());
      imageUrls.add(detail.getLargeUrl());
      imageUrls.add(detail.getMediumUrl());
    }

    // Delete files from storage
    if (!imageUrls.isEmpty()) {
      fileStorageService.deleteImages(imageUrls);
    }

    // Delete porto (cascade will delete details)
    portoRepository.delete(porto);

    log.info("Deleted portfolio {} with {} images", portoId, porto.getDetails().size());
  }

  @Transactional
  public PortoResponse addImages(Long portoId, List<MultipartFile> images) {
    // Verify ownership before adding images
    verifyPortoOwnership(portoId);

    Porto porto = portoRepository.findByIdWithDetails(portoId);

    if (porto == null) {
      throw new RuntimeException("Portfolio not found with id: " + portoId);
    }

    addImagesToPorto(porto, images);

    // Reload with updated details
    porto = portoRepository.findByIdWithDetails(porto.getId());

    return mapToPortoResponse(porto);
  }

  @Transactional
  public void deleteImage(Long imageId) {
    PortoDetail detail =
        portoDetailRepository
            .findById(imageId)
            .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

    // Verify ownership before deleting image
    verifyPortoOwnership(detail.getPorto().getId());

    // Delete files from storage
    fileStorageService.deleteImages(
        List.of(detail.getOriginalUrl(), detail.getLargeUrl(), detail.getMediumUrl()));

    // Delete from database
    portoDetailRepository.delete(detail);

    log.info("Deleted image {} from portfolio {}", imageId, detail.getPorto().getId());
  }

  private void addImagesToPorto(Porto porto, List<MultipartFile> images) {
    int currentMaxOrder =
        porto.getDetails().stream().mapToInt(PortoDetail::getDisplayOrder).max().orElse(-1);

    for (int i = 0; i < images.size(); i++) {
      MultipartFile file = images.get(i);

      Map<ImageSize, String> urlMap =
          fileStorageService.uploadImagePorto(file, porto.getArchitect().getId(), porto.getId());

      // Create PortoDetail entity
      PortoDetail detail =
          PortoDetail.builder()
              .porto(porto)
              .originalUrl(urlMap.get(ImageSize.ORIGINAL))
              .largeUrl(urlMap.get(ImageSize.LARGE))
              .mediumUrl(urlMap.get(ImageSize.MEDIUM))
              .displayOrder(currentMaxOrder + i + 1)
              .build();

      porto.getDetails().add(detail);
      portoDetailRepository.save(detail);
    }

    log.info("Added {} images to portfolio {}", images.size(), porto.getId());
  }

  private PortoResponse mapToPortoResponse(Porto porto) {
    List<PortoDetailResponse> imageResponses =
        porto.getDetails().stream()
            .sorted(Comparator.comparingInt(PortoDetail::getDisplayOrder))
            .map(this::mapToPortoDetailResponse)
            .collect(Collectors.toList());

    return PortoResponse.builder()
        .id(porto.getId())
        .architectId(porto.getArchitect().getId())
        .title(porto.getTitle())
        .description(porto.getDescription())
        .projectDate(porto.getProjectDate())
        .location(porto.getLocation())
        .projectType(porto.getProjectType())
        .isBuilt(porto.isBuilt())
        .images(imageResponses)
        .madeWithRumantra(Boolean.TRUE.equals(porto.getMadeWithRumantra()))
        .build();
  }

  private PortoDetailResponse mapToPortoDetailResponse(PortoDetail detail) {
    return PortoDetailResponse.builder()
        .id(detail.getId())
        .originalUrl(fileStorageService.getPublicUrl(detail.getOriginalUrl()))
        .largeUrl(fileStorageService.getPublicUrl(detail.getLargeUrl()))
        .mediumUrl(fileStorageService.getPublicUrl(detail.getMediumUrl()))
        .displayOrder(detail.getDisplayOrder())
        .build();
  }
}
