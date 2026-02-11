package com.rumantra.bidding.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.bidding.domain.BidImageType;
import com.rumantra.bidding.dto.BidAttachmentResponse;
import com.rumantra.bidding.dto.BidDetailRequest;
import com.rumantra.bidding.dto.BidImageResponse;
import com.rumantra.bidding.dto.BidQuotaResponse;
import com.rumantra.bidding.dto.BidResponse;
import com.rumantra.bidding.dto.CreateBidRequest;
import com.rumantra.bidding.dto.LinkPortfoliosRequest;
import com.rumantra.bidding.dto.UpdateBidRequest;
import com.rumantra.bidding.service.BidDetailService;
import com.rumantra.bidding.service.BidImageService;
import com.rumantra.bidding.service.BidQuotaService;
import com.rumantra.bidding.service.BidService;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

  private final BidService bidService;
  private final BidQuotaService bidQuotaService;
  private final BidDetailService bidDetailService;
  private final BidImageService bidImageService;
  private final com.rumantra.bidding.service.BidAttachmentService bidAttachmentService;
  private final ArchitectRepository architectRepository;

  @PostMapping
  public ResponseEntity<ApiResponse<BidResponse>> createDraftBid(
      @Valid @RequestBody CreateBidRequest request) {

    try {
      log.info("Creating draft bid on project ID: {}", request.getProjectId());

      BidResponse response = bidService.createDraftBid(request);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.<BidResponse>builder()
                  .success(true)
                  .message("Draft bid created successfully")
                  .data(response)
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (RuntimeException e) {
      log.error("Error creating draft bid: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<BidResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/{bidId}/submit")
  public ResponseEntity<ApiResponse<BidResponse>> submitBid(@PathVariable Long bidId) {

    try {
      log.info("Submitting bid ID: {}", bidId);

      BidResponse response = bidService.submitBid(bidId);

      return ResponseEntity.ok(
          ApiResponse.<BidResponse>builder()
              .success(true)
              .message("Bid submitted successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error submitting bid: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<BidResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PutMapping("/{bidId}")
  public ResponseEntity<ApiResponse<BidResponse>> updateDraftBid(
      @PathVariable Long bidId, @Valid @RequestBody UpdateBidRequest request) {

    try {
      log.info("Updating draft bid ID: {}", bidId);

      BidResponse response = bidService.updateDraftBid(bidId, request);

      return ResponseEntity.ok(
          ApiResponse.<BidResponse>builder()
              .success(true)
              .message("Draft bid updated successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error updating draft bid: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<BidResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PutMapping("/{bidId}/details")
  public ResponseEntity<ApiResponse<BidResponse>> updateBidDetails(
      @PathVariable Long bidId, @Valid @RequestBody BidDetailRequest request) {

    try {
      log.info("Updating bid details for bid ID: {}", bidId);

      BidResponse response = bidService.updateBidDetails(bidId, request);

      return ResponseEntity.ok(
          ApiResponse.<BidResponse>builder()
              .success(true)
              .message("Bid details updated successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error updating bid details: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<BidResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/{bidId}/concept-sketches")
  public ResponseEntity<ApiResponse<List<BidImageResponse>>> uploadConceptSketches(
      @PathVariable Long bidId, @RequestParam("images") List<MultipartFile> images) {

    try {
      log.info("Uploading {} concept sketches for bid ID: {}", images.size(), bidId);

      Long userId = SecurityUtils.getCurrentUserId();
      Architect architect =
          architectRepository
              .findByUserId(userId)
              .orElseThrow(() -> new RuntimeException("Please activate architect role first"));

      com.rumantra.bidding.domain.Bid bid = bidService.getBidEntityById(bidId, architect.getId());

      List<BidImageResponse> response =
          bidImageService.uploadImages(bid, BidImageType.CONCEPT_SKETCH, images);

      return ResponseEntity.ok(
          ApiResponse.<List<BidImageResponse>>builder()
              .success(true)
              .message("Concept sketches uploaded successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error uploading concept sketches: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<List<BidImageResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/{bidId}/mood-boards")
  public ResponseEntity<ApiResponse<List<BidImageResponse>>> uploadMoodBoards(
      @PathVariable Long bidId, @RequestParam("images") List<MultipartFile> images) {

    try {
      log.info("Uploading {} mood boards for bid ID: {}", images.size(), bidId);

      Long userId = SecurityUtils.getCurrentUserId();
      Architect architect =
          architectRepository
              .findByUserId(userId)
              .orElseThrow(() -> new RuntimeException("Please activate architect role first"));

      com.rumantra.bidding.domain.Bid bid = bidService.getBidEntityById(bidId, architect.getId());

      List<BidImageResponse> response =
          bidImageService.uploadImages(bid, BidImageType.MOOD_BOARD, images);

      return ResponseEntity.ok(
          ApiResponse.<List<BidImageResponse>>builder()
              .success(true)
              .message("Mood boards uploaded successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error uploading mood boards: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<List<BidImageResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/{bidId}/portfolios")
  public ResponseEntity<ApiResponse<BidResponse>> linkPortfolios(
      @PathVariable Long bidId, @Valid @RequestBody LinkPortfoliosRequest request) {

    try {
      log.info("Linking {} portfolios to bid ID: {}", request.getPortfolioIds().size(), bidId);

      BidResponse response = bidService.linkPortfolios(bidId, request);

      return ResponseEntity.ok(
          ApiResponse.<BidResponse>builder()
              .success(true)
              .message("Portfolios linked successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error linking portfolios: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<BidResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @DeleteMapping("/images/{imageId}")
  public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long imageId) {

    try {
      log.info("Deleting bid image ID: {}", imageId);

      bidImageService.deleteImage(imageId);

      return ResponseEntity.ok(
          ApiResponse.<Void>builder()
              .success(true)
              .message("Image deleted successfully")
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error deleting image: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Get all bids for the authenticated architect
   *
   * @return List of bids
   */
  @GetMapping("/my-bids")
  public ResponseEntity<ApiResponse<List<BidResponse>>> getMyBids() {
    try {
      Long userId = SecurityUtils.getCurrentUserId();
      Architect architect =
          architectRepository
              .findByUserId(userId)
              .orElseThrow(() -> new RuntimeException("Please activate architect role first"));

      List<BidResponse> bids = bidService.getBidsByArchitect(architect.getId());

      return ResponseEntity.ok(
          ApiResponse.<List<BidResponse>>builder()
              .success(true)
              .message("Bids retrieved successfully")
              .data(bids)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error retrieving bids: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<List<BidResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Get a specific bid by ID
   *
   * @param bidId Bid ID
   * @return Bid details
   */
  @GetMapping("/{bidId}")
  public ResponseEntity<ApiResponse<BidResponse>> getBidById(@PathVariable Long bidId) {
    try {
      BidResponse response = bidService.getBidById(bidId);

      return ResponseEntity.ok(
          ApiResponse.<BidResponse>builder()
              .success(true)
              .message("Bid retrieved successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error retrieving bid: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<BidResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Withdraw a pending bid
   *
   * @param bidId Bid ID to withdraw
   * @return Success message
   */
  @PutMapping("/{bidId}/withdraw")
  public ResponseEntity<ApiResponse<Void>> withdrawBid(@PathVariable Long bidId) {
    try {
      log.info("Withdrawing bid ID: {}", bidId);
      bidService.withdrawBid(bidId);

      return ResponseEntity.ok(
          ApiResponse.<Void>builder()
              .success(true)
              .message("Bid withdrawn successfully")
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error withdrawing bid: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Get bid quota information for the authenticated architect
   *
   * @return Bid quota details
   */
  @GetMapping("/quota")
  public ResponseEntity<ApiResponse<BidQuotaResponse>> getBidQuota() {
    try {
      Long userId = SecurityUtils.getCurrentUserId();
      Architect architect =
          architectRepository
              .findByUserId(userId)
              .orElseThrow(() -> new RuntimeException("Please activate architect role first"));

      BidQuotaResponse response = bidQuotaService.getQuotaResponse(architect.getId());

      return ResponseEntity.ok(
          ApiResponse.<BidQuotaResponse>builder()
              .success(true)
              .message("Quota retrieved successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error retrieving quota: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<BidQuotaResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/{bidId}/accept")
  public ResponseEntity<ApiResponse<BidResponse>> acceptBid(@PathVariable Long bidId) {
    try {
      log.info("Accepting bid ID: {}", bidId);

      BidResponse response = bidService.acceptBid(bidId);

      return ResponseEntity.ok(
          ApiResponse.<BidResponse>builder()
              .success(true)
              .message("Bid accepted successfully. Conversation created.")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error accepting bid: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<BidResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/{bidId}/attachments")
  public ResponseEntity<ApiResponse<BidAttachmentResponse>> uploadAttachment(
      @PathVariable Long bidId, @RequestParam("file") MultipartFile file) {

    try {
      log.info("Uploading PDF attachment for bid ID: {}", bidId);

      Long userId = SecurityUtils.getCurrentUserId();
      Architect architect =
          architectRepository
              .findByUserId(userId)
              .orElseThrow(() -> new RuntimeException("Please activate architect role first"));

      com.rumantra.bidding.domain.Bid bid = bidService.getBidEntityById(bidId, architect.getId());

      BidAttachmentResponse response = bidAttachmentService.uploadAttachment(bid, file);

      return ResponseEntity.ok(
          ApiResponse.<BidAttachmentResponse>builder()
              .success(true)
              .message("Attachment uploaded successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error uploading attachment: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<BidAttachmentResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @DeleteMapping("/attachments/{attachmentId}")
  public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable Long attachmentId) {

    try {
      log.info("Deleting bid attachment ID: {}", attachmentId);

      bidAttachmentService.deleteAttachment(attachmentId);

      return ResponseEntity.ok(
          ApiResponse.<Void>builder()
              .success(true)
              .message("Attachment deleted successfully")
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error deleting attachment: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @GetMapping("/{bidId}/attachments")
  public ResponseEntity<ApiResponse<List<BidAttachmentResponse>>> getAttachments(
      @PathVariable Long bidId) {

    try {
      List<BidAttachmentResponse> attachments = bidAttachmentService.getAttachments(bidId);

      return ResponseEntity.ok(
          ApiResponse.<List<BidAttachmentResponse>>builder()
              .success(true)
              .message("Attachments retrieved successfully")
              .data(attachments)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (RuntimeException e) {
      log.error("Error retrieving attachments: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<List<BidAttachmentResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }
}
