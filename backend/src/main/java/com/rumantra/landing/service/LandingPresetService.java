package com.rumantra.landing.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.landing.domain.LandingPreset;
import com.rumantra.landing.dto.PresetRequest;
import com.rumantra.landing.dto.PresetResponse;
import com.rumantra.landing.repository.LandingPresetRepository;
import com.rumantra.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandingPresetService {

  private final LandingPresetRepository presetRepository;

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
    LandingPreset preset =
        presetRepository
            .findById(presetId)
            .orElseThrow(() -> new ResourceNotFoundException("Preset not found: " + presetId));
    presetRepository.delete(preset);
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
        .displayOrder(preset.getDisplayOrder())
        .active(preset.isActive())
        .build();
  }
}
