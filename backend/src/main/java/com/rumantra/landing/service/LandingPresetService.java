package com.rumantra.landing.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.landing.domain.LandingPreset;
import com.rumantra.landing.dto.PresetRequest;
import com.rumantra.landing.dto.PresetResponse;
import com.rumantra.landing.repository.LandingPresetRepository;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.shared.storage.FileStorageService;
import com.rumantra.shared.storage.ImageSize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandingPresetService {

  // Presets have no owning architect; -1 keeps their images in a folder of their own, apart
  // from hero slides (0) and every real architect's portfolio uploads.
  private static final Long STORAGE_NAMESPACE = -1L;

  private final LandingPresetRepository presetRepository;
  private final FileStorageService fileStorageService;

  @Transactional(readOnly = true)
  public List<PresetResponse> listPublic() {
    return presetRepository.findAllByActiveTrueOrderByDisplayOrderAsc().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PresetResponse> listAll() {
    return presetRepository.findAllByOrderByDisplayOrderAsc().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public PresetResponse create(PresetRequest request) {
    if (presetRepository.existsBySlug(request.getSlug())) {
      throw new IllegalStateException(
          "A preset with slug '" + request.getSlug() + "' already exists");
    }

    int nextOrder =
        presetRepository.findAllByOrderByDisplayOrderAsc().stream()
                .mapToInt(LandingPreset::getDisplayOrder)
                .max()
                .orElse(0)
            + 1;

    LandingPreset preset = LandingPreset.builder().displayOrder(nextOrder).build();
    applyRequest(preset, request);
    return mapToResponse(presetRepository.save(preset));
  }

  @Transactional
  public PresetResponse update(Long presetId, PresetRequest request) {
    LandingPreset preset =
        presetRepository
            .findById(presetId)
            .orElseThrow(() -> new ResourceNotFoundException("Preset not found: " + presetId));

    presetRepository
        .findBySlug(request.getSlug())
        .filter(existing -> !existing.getId().equals(presetId))
        .ifPresent(
            existing -> {
              throw new IllegalStateException(
                  "A preset with slug '" + request.getSlug() + "' already exists");
            });

    applyRequest(preset, request);
    return mapToResponse(presetRepository.save(preset));
  }

  @Transactional
  public void delete(Long presetId) {
    LandingPreset preset = findPreset(presetId);
    deleteStoredImages(storedImageUrls(preset));
    presetRepository.delete(preset);
  }

  @Transactional
  public PresetResponse uploadImage(Long presetId, MultipartFile image) {
    if (image == null || image.isEmpty()) {
      throw new IllegalArgumentException("Image file is required");
    }
    LandingPreset preset = findPreset(presetId);
    List<String> previous = storedImageUrls(preset);

    // Upload before deleting, so a failed upload leaves the preset with its old image rather
    // than none.
    Map<ImageSize, String> urls =
        fileStorageService.uploadImagePorto(image, STORAGE_NAMESPACE, preset.getId());
    preset.setImageOriginalUrl(urls.get(ImageSize.ORIGINAL));
    preset.setImageLargeUrl(urls.get(ImageSize.LARGE));
    preset.setImageMediumUrl(urls.get(ImageSize.MEDIUM));
    preset = presetRepository.save(preset);

    deleteStoredImages(previous);
    return mapToResponse(preset);
  }

  @Transactional
  public PresetResponse removeImage(Long presetId) {
    LandingPreset preset = findPreset(presetId);
    deleteStoredImages(storedImageUrls(preset));
    preset.setImageOriginalUrl(null);
    preset.setImageLargeUrl(null);
    preset.setImageMediumUrl(null);
    return mapToResponse(presetRepository.save(preset));
  }

  @Transactional
  public List<PresetResponse> reorder(List<Long> orderedIds) {
    List<LandingPreset> presets = presetRepository.findAllById(orderedIds);
    Map<Long, LandingPreset> byId =
        presets.stream().collect(Collectors.toMap(LandingPreset::getId, p -> p));

    for (int i = 0; i < orderedIds.size(); i++) {
      LandingPreset preset = byId.get(orderedIds.get(i));
      if (preset == null) {
        throw new ResourceNotFoundException("Preset not found: " + orderedIds.get(i));
      }
      preset.setDisplayOrder(i + 1);
    }

    presetRepository.saveAll(presets);
    return listAll();
  }

  private LandingPreset findPreset(Long presetId) {
    return presetRepository
        .findById(presetId)
        .orElseThrow(() -> new ResourceNotFoundException("Preset not found: " + presetId));
  }

  private List<String> storedImageUrls(LandingPreset preset) {
    return Stream.of(
            preset.getImageOriginalUrl(), preset.getImageLargeUrl(), preset.getImageMediumUrl())
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private void deleteStoredImages(List<String> urls) {
    if (!urls.isEmpty()) {
      fileStorageService.deleteImages(urls);
    }
  }

  private String toPublicUrl(String storedPath) {
    return storedPath == null ? null : fileStorageService.getPublicUrl(storedPath);
  }

  private void applyRequest(LandingPreset preset, PresetRequest request) {
    preset.setSlug(request.getSlug());
    preset.setLabelEn(request.getLabelEn());
    preset.setLabelId(request.getLabelId());
    preset.setEyebrowEn(request.getEyebrowEn());
    preset.setEyebrowId(request.getEyebrowId());
    preset.setIconName(request.getIconName());
    preset.setBuildingFunction(request.getBuildingFunction());
    preset.setProjectScope(request.getProjectScope());
    preset.setSubCategory(request.getSubCategory());
    preset.setDefaultTitleEn(request.getDefaultTitleEn());
    preset.setDefaultTitleId(request.getDefaultTitleId());
    preset.setDefaultLotSize(request.getDefaultLotSize());
    preset.setDefaultDesignBudget(request.getDefaultDesignBudget());
    preset.setDefaultDescriptionEn(request.getDefaultDescriptionEn());
    preset.setDefaultDescriptionId(request.getDefaultDescriptionId());
    if (request.getActive() != null) {
      preset.setActive(request.getActive());
    }
  }

  private PresetResponse mapToResponse(LandingPreset preset) {
    return PresetResponse.builder()
        .id(preset.getId())
        .slug(preset.getSlug())
        .labelEn(preset.getLabelEn())
        .labelId(preset.getLabelId())
        .eyebrowEn(preset.getEyebrowEn())
        .eyebrowId(preset.getEyebrowId())
        .iconName(preset.getIconName())
        .buildingFunction(preset.getBuildingFunction())
        .projectScope(preset.getProjectScope())
        .subCategory(preset.getSubCategory())
        .defaultTitleEn(preset.getDefaultTitleEn())
        .defaultTitleId(preset.getDefaultTitleId())
        .defaultLotSize(preset.getDefaultLotSize())
        .defaultDesignBudget(preset.getDefaultDesignBudget())
        .defaultDescriptionEn(preset.getDefaultDescriptionEn())
        .defaultDescriptionId(preset.getDefaultDescriptionId())
        .imageUrl(toPublicUrl(preset.getImageMediumUrl()))
        .imageLargeUrl(toPublicUrl(preset.getImageLargeUrl()))
        .displayOrder(preset.getDisplayOrder())
        .active(preset.isActive())
        .build();
  }
}
