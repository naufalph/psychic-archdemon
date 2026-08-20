package com.rumantra.landing.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.landing.dto.HeroSlideReorderRequest;
import com.rumantra.landing.dto.HeroSlideRequest;
import com.rumantra.landing.dto.HeroSlideResponse;
import com.rumantra.landing.service.LandingHeroService;
import com.rumantra.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Landing")
@RestController
@RequestMapping("/rmtr/admin/landing")
@RequiredArgsConstructor
public class AdminLandingHeroController {

  private final LandingHeroService landingHeroService;

  @GetMapping("/hero-slides")
  public ResponseEntity<ApiResponse<List<HeroSlideResponse>>> getHeroSlides() {
    return ResponseEntity.ok(ApiResponse.success(landingHeroService.listAll()));
  }

  @PostMapping(value = "/hero-slides", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<HeroSlideResponse>> createHeroSlide(
      @Valid @ModelAttribute HeroSlideRequest request,
      @RequestParam(value = "image", required = false) MultipartFile image) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(landingHeroService.create(request, image)));
  }

  @PutMapping(value = "/hero-slides/{slideId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<HeroSlideResponse>> updateHeroSlide(
      @PathVariable Long slideId,
      @Valid @ModelAttribute HeroSlideRequest request,
      @RequestParam(value = "image", required = false) MultipartFile image) {
    return ResponseEntity.ok(
        ApiResponse.success(landingHeroService.update(slideId, request, image)));
  }

  @DeleteMapping("/hero-slides/{slideId}")
  public ResponseEntity<ApiResponse<Void>> deleteHeroSlide(@PathVariable Long slideId) {
    landingHeroService.delete(slideId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping("/hero-slides/reorder")
  public ResponseEntity<ApiResponse<List<HeroSlideResponse>>> reorderHeroSlides(
      @Valid @RequestBody HeroSlideReorderRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(landingHeroService.reorder(request.getOrderedIds())));
  }
}
