package com.rumantra.university.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rumantra.shared.dto.ApiResponse;
import com.rumantra.university.dto.UniversityResponse;
import com.rumantra.university.service.UniversityService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "University")
@RestController
@RequestMapping("/rmtr/universities")
@RequiredArgsConstructor
public class UniversityController {

  private final UniversityService universityService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<UniversityResponse>>> list(
      @RequestParam(required = false) String search) {
    return ResponseEntity.ok(ApiResponse.success(universityService.list(search)));
  }
}
