package com.rumantra.landing.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.landing.domain.LandingHeroSlide;
import com.rumantra.landing.dto.HeroSlideRequest;
import com.rumantra.landing.dto.HeroSlideResponse;
import com.rumantra.landing.repository.LandingHeroSlideRepository;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.shared.storage.FileStorageService;
import com.rumantra.shared.storage.ImageSize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandingHeroService {

  // Hero slides have no owning architect; 0 keeps them in their own storage folder,
  // away from every real architect's portfolio uploads.
  private static final Long STORAGE_NAMESPACE = 0L;

  private final LandingHeroSlideRepository slideRepository;
  private final FileStorageService fileStorageService;

  @Transactional(readOnly = true)
  public List<HeroSlideResponse> listPublic() {
    return slideRepository.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<HeroSlideResponse> listAll() {
    return slideRepository.findAllByOrderByDisplayOrderAsc().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public HeroSlideResponse create(HeroSlideRequest request, MultipartFile image) {
    int nextOrder =
        slideRepository.findAllByOrderByDisplayOrderAsc().stream()
                .mapToInt(LandingHeroSlide::getDisplayOrder)
                .max()
                .orElse(0)
            + 1;

    LandingHeroSlide slide = LandingHeroSlide.builder().displayOrder(nextOrder).build();
    applyRequest(slide, request);
    slide = slideRepository.save(slide);

    if (image != null && !image.isEmpty()) {
      storeImage(slide, image);
      slide = slideRepository.save(slide);
    }

    log.info("Created landing hero slide {}", slide.getId());
    return mapToResponse(slide);
  }

  @Transactional
  public HeroSlideResponse update(Long slideId, HeroSlideRequest request, MultipartFile image) {
    LandingHeroSlide slide = findSlide(slideId);
    applyRequest(slide, request);

    boolean replacingImage = image != null && !image.isEmpty();
    if (replacingImage || Boolean.TRUE.equals(request.getRemoveImage())) {
      deleteStoredImage(slide);
    }
    if (replacingImage) {
      storeImage(slide, image);
    }

    slide = slideRepository.save(slide);
    log.info("Updated landing hero slide {}", slideId);
    return mapToResponse(slide);
  }

  @Transactional
  public void delete(Long slideId) {
    LandingHeroSlide slide = findSlide(slideId);
    deleteStoredImage(slide);
    slideRepository.delete(slide);
    log.info("Deleted landing hero slide {}", slideId);
  }

  @Transactional
  public List<HeroSlideResponse> reorder(List<Long> orderedIds) {
    List<LandingHeroSlide> slides = slideRepository.findAllById(orderedIds);
    Map<Long, LandingHeroSlide> byId =
        slides.stream().collect(Collectors.toMap(LandingHeroSlide::getId, s -> s));

    for (int i = 0; i < orderedIds.size(); i++) {
      LandingHeroSlide slide = byId.get(orderedIds.get(i));
      if (slide == null) {
        throw new ResourceNotFoundException("Hero slide not found: " + orderedIds.get(i));
      }
      slide.setDisplayOrder(i + 1);
    }

    slideRepository.saveAll(slides);
    return listAll();
  }

  private LandingHeroSlide findSlide(Long slideId) {
    return slideRepository
        .findById(slideId)
        .orElseThrow(() -> new ResourceNotFoundException("Hero slide not found: " + slideId));
  }

  private void applyRequest(LandingHeroSlide slide, HeroSlideRequest request) {
    slide.setArchitectName(request.getArchitectName().trim());
    slide.setAvatarInitial(resolveInitial(request));
    slide.setRating(request.getRating());
    slide.setReviewQuote(trimToNull(request.getReviewQuote()));
    slide.setReviewerName(trimToNull(request.getReviewerName()));
    if (request.getVerified() != null) {
      slide.setVerified(request.getVerified());
    }
    if (request.getActive() != null) {
      slide.setActive(request.getActive());
    }
  }

  private String resolveInitial(HeroSlideRequest request) {
    String initial = trimToNull(request.getAvatarInitial());
    if (initial != null) {
      return initial.toUpperCase();
    }
    String name = request.getArchitectName().trim();
    return name.isEmpty() ? null : name.substring(0, 1).toUpperCase();
  }

  private void storeImage(LandingHeroSlide slide, MultipartFile image) {
    Map<ImageSize, String> urls =
        fileStorageService.uploadImagePorto(image, STORAGE_NAMESPACE, slide.getId());
    slide.setImageOriginalUrl(urls.get(ImageSize.ORIGINAL));
    slide.setImageLargeUrl(urls.get(ImageSize.LARGE));
    slide.setImageMediumUrl(urls.get(ImageSize.MEDIUM));
  }

  private void deleteStoredImage(LandingHeroSlide slide) {
    List<String> urls = new ArrayList<>();
    if (slide.getImageOriginalUrl() != null) urls.add(slide.getImageOriginalUrl());
    if (slide.getImageLargeUrl() != null) urls.add(slide.getImageLargeUrl());
    if (slide.getImageMediumUrl() != null) urls.add(slide.getImageMediumUrl());

    if (!urls.isEmpty()) {
      fileStorageService.deleteImages(urls);
    }

    slide.setImageOriginalUrl(null);
    slide.setImageLargeUrl(null);
    slide.setImageMediumUrl(null);
  }

  private HeroSlideResponse mapToResponse(LandingHeroSlide slide) {
    return HeroSlideResponse.builder()
        .id(slide.getId())
        .imageUrl(toPublicUrl(slide.getImageMediumUrl()))
        .imageLargeUrl(toPublicUrl(slide.getImageLargeUrl()))
        .architectName(slide.getArchitectName())
        .avatarInitial(slide.getAvatarInitial())
        .verified(slide.isVerified())
        .rating(slide.getRating())
        .reviewQuote(slide.getReviewQuote())
        .reviewerName(slide.getReviewerName())
        .displayOrder(slide.getDisplayOrder())
        .active(slide.isActive())
        .build();
  }

  private String toPublicUrl(String storedPath) {
    return storedPath == null ? null : fileStorageService.getPublicUrl(storedPath);
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
