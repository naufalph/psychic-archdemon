package com.rumantra.architect.service;

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

    // Mark onboarding as complete on first portfolio
    Architect fullArchitect =
        architectRepository
            .findById(architectId)
            .orElseThrow(() -> new RuntimeException("Architect not found"));

    if (fullArchitect.getNeedsOnboarding() != null && fullArchitect.getNeedsOnboarding()) {
      fullArchitect.setNeedsOnboarding(false);
      fullArchitect.setOnboardingCompletedAt(java.sql.Timestamp.from(java.time.Instant.now()));
      architectRepository.save(fullArchitect);
      log.info("Architect {} completed onboarding", architectId);
    }

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

    // Update fields if provided
    if (request.getTitle() != null) {
      porto.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
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
        .build();
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
