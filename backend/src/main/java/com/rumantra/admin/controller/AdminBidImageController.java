package com.rumantra.admin.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rumantra.bidding.service.BidImageArchiveService;
import com.rumantra.bidding.service.BidImageArchiveService.ArchiveResult;
import com.rumantra.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Bid Images")
@RestController
@RequestMapping("/rmtr/admin/bid-images")
@RequiredArgsConstructor
public class AdminBidImageController {

  private final BidImageArchiveService bidImageArchiveService;

  /** Runs the retention sweep on demand. Honours the configured dry-run flag. */
  @PostMapping("/archive")
  public ResponseEntity<ApiResponse<Map<String, Object>>> archive() {
    ArchiveResult result = bidImageArchiveService.archiveExpiredImages();

    return ResponseEntity.ok(
        ApiResponse.success(
            Map.of(
                "dryRun", result.isDryRun(),
                "imageCount", result.getImageCount(),
                "totalBytes", result.getTotalBytes(),
                "cutoff", result.getCutoff().toString())));
  }
}
