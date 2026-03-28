package com.rumantra.architect.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.architect.dto.*;
import com.rumantra.architect.service.PortoService;
import com.rumantra.shared.dto.ApiResponse;
import com.rumantra.shared.storage.StorageException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rmtr/porto")
@RequiredArgsConstructor
public class PortoController {

  private final PortoService portoService;

  /**
   * Create a new portfolio for the authenticated architect with images.
   *
   * @param request Portfolio metadata
   * @param images List of image files (optional)
   * @return Created portfolio
   */
  @PostMapping(value = "/portos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<PortoResponse>> createPorto(
      @Valid @ModelAttribute CreatePortoRequest request,
      @RequestParam(value = "images", required = false) List<MultipartFile> images) {

    try {
      log.info("Creating portfolio with {} images", images != null ? images.size() : 0);

      PortoResponse response = portoService.createPorto(request, images);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(true)
                  .message("Portfolio created successfully!")
                  .data(response)
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (StorageException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (RuntimeException e) {
      log.error("Error creating portfolio", e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (Exception e) {
      log.error("Unexpected error creating portfolio", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message("An error occurred while creating portfolio")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Get all portfolios for the authenticated architect. Returns lightweight responses with first
   * image for each portfolio.
   *
   * @return List of portfolios
   */
  @GetMapping("/portos")
  public ResponseEntity<ApiResponse<List<PortoListResponse>>> getPortosByArchitect() {

    try {
      log.info("Fetching portfolios for authenticated architect");

      List<PortoListResponse> response = portoService.getPortosByArchitect();

      return ResponseEntity.ok(
          ApiResponse.<List<PortoListResponse>>builder()
              .success(true)
              .message("Portfolios retrieved successfully!")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              ApiResponse.<List<PortoListResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (RuntimeException e) {
      log.error("Error fetching portfolios for authenticated architect", e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<List<PortoListResponse>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (Exception e) {
      log.error("Unexpected error fetching portfolios for authenticated architect", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<List<PortoListResponse>>builder()
                  .success(false)
                  .message("An error occurred while fetching portfolios")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Get a single portfolio by ID with all images.
   *
   * @param portoId The portfolio ID
   * @return Portfolio details
   */
  @GetMapping("/portos/{portoId}")
  public ResponseEntity<ApiResponse<PortoResponse>> getPortoById(@PathVariable Long portoId) {

    try {
      log.info("Fetching portfolio {}", portoId);

      PortoResponse response = portoService.getPortoById(portoId);

      return ResponseEntity.ok(
          ApiResponse.<PortoResponse>builder()
              .success(true)
              .message("Portfolio retrieved successfully!")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (RuntimeException e) {
      log.error("Error fetching portfolio {}", portoId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (Exception e) {
      log.error("Unexpected error fetching portfolio {}", portoId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message("An error occurred while fetching portfolio")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Update portfolio metadata (not images).
   *
   * @param portoId The portfolio ID
   * @param request Updated metadata
   * @return Updated portfolio
   */
  @PutMapping("/portos/{portoId}")
  public ResponseEntity<ApiResponse<PortoResponse>> updatePorto(
      @PathVariable Long portoId, @Valid @RequestBody UpdatePortoRequest request) {

    try {
      log.info("Updating portfolio {}", portoId);

      PortoResponse response = portoService.updatePorto(portoId, request);

      return ResponseEntity.ok(
          ApiResponse.<PortoResponse>builder()
              .success(true)
              .message("Portfolio updated successfully!")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (RuntimeException e) {
      log.error("Error updating portfolio {}", portoId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (Exception e) {
      log.error("Unexpected error updating portfolio {}", portoId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message("An error occurred while updating portfolio")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Delete a portfolio and all its images.
   *
   * @param portoId The portfolio ID
   * @return Success message
   */
  @DeleteMapping("/portos/{portoId}")
  public ResponseEntity<ApiResponse<Void>> deletePorto(@PathVariable Long portoId) {

    try {
      log.info("Deleting portfolio {}", portoId);

      portoService.deletePorto(portoId);

      return ResponseEntity.ok(
          ApiResponse.<Void>builder()
              .success(true)
              .message("Portfolio deleted successfully!")
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (StorageException e) {
      log.error("Error deleting portfolio files for {}", portoId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message("Error deleting portfolio files: " + e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (RuntimeException e) {
      log.error("Error deleting portfolio {}", portoId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (Exception e) {
      log.error("Unexpected error deleting portfolio {}", portoId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message("An error occurred while deleting portfolio")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Add images to an existing portfolio.
   *
   * @param portoId The portfolio ID
   * @param images List of image files
   * @return Updated portfolio
   */
  @PostMapping(value = "/portos/{portoId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<PortoResponse>> addImages(
      @PathVariable Long portoId, @RequestParam("images") List<MultipartFile> images) {

    try {
      log.info("Adding {} images to portfolio {}", images.size(), portoId);

      PortoResponse response = portoService.addImages(portoId, images);

      return ResponseEntity.ok(
          ApiResponse.<PortoResponse>builder()
              .success(true)
              .message("Images added successfully!")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (StorageException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (RuntimeException e) {
      log.error("Error adding images to portfolio {}", portoId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (Exception e) {
      log.error("Unexpected error adding images to portfolio {}", portoId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<PortoResponse>builder()
                  .success(false)
                  .message("An error occurred while adding images")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Delete a specific image from a portfolio.
   *
   * @param imageId The image ID
   * @return Success message
   */
  @DeleteMapping("/portos/images/{imageId}")
  public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long imageId) {

    try {
      log.info("Deleting image {}", imageId);

      portoService.deleteImage(imageId);

      return ResponseEntity.ok(
          ApiResponse.<Void>builder()
              .success(true)
              .message("Image deleted successfully!")
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (AccessDeniedException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (StorageException e) {
      log.error("Error deleting image file for {}", imageId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message("Error deleting image file: " + e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (RuntimeException e) {
      log.error("Error deleting image {}", imageId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (Exception e) {
      log.error("Unexpected error deleting image {}", imageId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message("An error occurred while deleting image")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }
}
