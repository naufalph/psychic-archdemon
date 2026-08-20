package com.rumantra.landing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.landing.dto.BriefRequest;
import com.rumantra.landing.dto.BriefResponse;
import com.rumantra.landing.dto.PresetResponse;
import com.rumantra.landing.service.LandingBriefService;
import com.rumantra.landing.service.LandingPresetService;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Landing")
@RestController
@RequestMapping("/rmtr/landing")
@RequiredArgsConstructor
public class LandingPresetController {

  private final LandingPresetService presetService;
  private final LandingBriefService briefService;

  @GetMapping("/presets")
  public ResponseEntity<ApiResponse<List<PresetResponse>>> getPresets() {
    return ResponseEntity.ok(ApiResponse.success(presetService.listPublic()));
  }

  @PostMapping("/briefs")
  public ResponseEntity<ApiResponse<BriefResponse>> createBrief(
      @Valid @RequestBody BriefRequest request, HttpServletRequest httpRequest) {
    return ResponseEntity.ok(
        ApiResponse.success(briefService.create(request, resolveClientIp(httpRequest))));
  }

  @PostMapping("/briefs/{claimToken}/claim")
  public ResponseEntity<ApiResponse<BriefResponse>> claimBrief(@PathVariable String claimToken) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(ApiResponse.success(briefService.claim(claimToken, userId)));
  }

  /** Fallback for users who verified their email on a device that never held the claim token. */
  @PostMapping("/briefs/mine/consume")
  public ResponseEntity<ApiResponse<BriefResponse>> consumeMyBrief() {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(ApiResponse.success(briefService.consumeMine(userId)));
  }

  private String resolveClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
