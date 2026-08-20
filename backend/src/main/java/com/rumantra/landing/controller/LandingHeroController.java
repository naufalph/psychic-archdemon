package com.rumantra.landing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rumantra.landing.dto.HeroSlideResponse;
import com.rumantra.landing.service.LandingHeroService;
import com.rumantra.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Landing")
@RestController
@RequestMapping("/rmtr/landing")
@RequiredArgsConstructor
public class LandingHeroController {

  private final LandingHeroService landingHeroService;

  @GetMapping("/hero-slides")
  public ResponseEntity<ApiResponse<List<HeroSlideResponse>>> getHeroSlides() {
    return ResponseEntity.ok(ApiResponse.success(landingHeroService.listPublic()));
  }
}
