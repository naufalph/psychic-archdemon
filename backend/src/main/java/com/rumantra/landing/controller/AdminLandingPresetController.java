package com.rumantra.landing.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.landing.dto.PresetReorderRequest;
import com.rumantra.landing.dto.PresetRequest;
import com.rumantra.landing.dto.PresetResponse;
import com.rumantra.landing.service.LandingPresetService;
import com.rumantra.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Landing")
@RestController
@RequestMapping("/rmtr/admin/landing")
@RequiredArgsConstructor
public class AdminLandingPresetController {

  private final LandingPresetService presetService;

  @GetMapping("/presets")
  public ResponseEntity<ApiResponse<List<PresetResponse>>> getPresets() {
    return ResponseEntity.ok(ApiResponse.success(presetService.listAll()));
  }

  @PostMapping("/presets")
  public ResponseEntity<ApiResponse<PresetResponse>> createPreset(
      @Valid @RequestBody PresetRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(presetService.create(request)));
  }

  @PutMapping("/presets/reorder")
  public ResponseEntity<ApiResponse<List<PresetResponse>>> reorderPresets(
      @Valid @RequestBody PresetReorderRequest request) {
    return ResponseEntity.ok(ApiResponse.success(presetService.reorder(request.getOrderedIds())));
  }

  @PutMapping("/presets/{presetId}")
  public ResponseEntity<ApiResponse<PresetResponse>> updatePreset(
      @PathVariable Long presetId, @Valid @RequestBody PresetRequest request) {
    return ResponseEntity.ok(ApiResponse.success(presetService.update(presetId, request)));
  }

  @DeleteMapping("/presets/{presetId}")
  public ResponseEntity<ApiResponse<Void>> deletePreset(@PathVariable Long presetId) {
    presetService.delete(presetId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
