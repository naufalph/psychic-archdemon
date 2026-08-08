package com.rumantra.client.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.bidding.dto.BidResponse;
import com.rumantra.client.dto.CreateProjectRequest;
import com.rumantra.client.dto.ProjectFileDto;
import com.rumantra.client.dto.ProjectPublicPreviewResponse;
import com.rumantra.client.dto.ProjectResponse;
import com.rumantra.client.dto.UpdateValidationRequest;
import com.rumantra.client.service.ProjectService;
import com.rumantra.shared.dto.ApiResponse;
import com.rumantra.shared.exception.ResourceNotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rmtr/projects")
@RequiredArgsConstructor
public class ProjectController {

  private final ProjectService projectService;

  @PostMapping
  public ResponseEntity<ApiResponse<ProjectResponse>> createDraftProject(
      @Valid @RequestBody CreateProjectRequest request) {

    try {

      ProjectResponse response = projectService.createDraftProject(request);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(true)
                  .message("Draft project saved")
                  .data(response)
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (ResourceNotFoundException e) {
      log.error("Error creating draft project", e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (Exception e) {
      log.error("Unexpected error creating draft project", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message("An error occurred while creating project")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PutMapping("/{projectId}")
  public ResponseEntity<ApiResponse<ProjectResponse>> updateDraftProject(
      @PathVariable Long projectId, @Valid @RequestBody CreateProjectRequest request) {

    try {

      ProjectResponse response = projectService.updateDraftProject(projectId, request);

      return ResponseEntity.ok(
          ApiResponse.<ProjectResponse>builder()
              .success(true)
              .message("Draft project updated")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (ResourceNotFoundException e) {
      log.error("Project not found: {}", projectId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (IllegalStateException e) {
      log.warn("Cannot update project {}: {}", projectId, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (Exception e) {
      log.error("Unexpected error updating draft project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message("An error occurred while updating project")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping(value = "/{projectId}/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<ProjectResponse>> submitProject(
      @PathVariable Long projectId,
      @RequestParam(value = "files", required = false) List<MultipartFile> files) {

    try {

      ProjectResponse response = projectService.submitProject(projectId, files);

      return ResponseEntity.ok(
          ApiResponse.<ProjectResponse>builder()
              .success(true)
              .message("Project submitted for approval!")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (IllegalArgumentException e) {
      log.warn("Validation error submitting project {}: {}", projectId, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (IllegalStateException e) {
      log.warn("Cannot submit project {}: {}", projectId, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (ResourceNotFoundException e) {
      log.error("Project not found: {}", projectId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (Exception e) {
      log.error("Unexpected error submitting project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message("An error occurred while submitting project")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping(value = "/{projectId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<List<ProjectFileDto>>> uploadDraftFiles(
      @PathVariable Long projectId, @RequestParam("files") List<MultipartFile> files) {

    try {

      List<ProjectFileDto> response = projectService.uploadDraftFiles(projectId, files);

      return ResponseEntity.ok(
          ApiResponse.<List<ProjectFileDto>>builder()
              .success(true)
              .message("Images uploaded")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (ResourceNotFoundException e) {
      log.error("Project not found: {}", projectId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<List<ProjectFileDto>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (IllegalStateException e) {
      log.warn("Cannot upload files to project {}: {}", projectId, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<List<ProjectFileDto>>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (Exception e) {
      log.error("Unexpected error uploading files to project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<List<ProjectFileDto>>builder()
                  .success(false)
                  .message("An error occurred while uploading images")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @DeleteMapping("/{projectId}/files/{fileId}")
  public ResponseEntity<ApiResponse<Void>> deleteProjectFile(
      @PathVariable Long projectId, @PathVariable Long fileId) {

    try {

      projectService.deleteProjectFile(projectId, fileId);

      return ResponseEntity.ok(
          ApiResponse.<Void>builder()
              .success(true)
              .message("Image removed")
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (ResourceNotFoundException e) {
      log.error("Project or file not found: {} / {}", projectId, fileId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (IllegalStateException | IllegalArgumentException e) {
      log.warn("Cannot delete file {} from project {}: {}", fileId, projectId, e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (Exception e) {
      log.error("Unexpected error deleting file {} from project {}", fileId, projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message("An error occurred while removing the image")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @GetMapping("/{projectId}")
  public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long projectId) {

    try {
      ProjectResponse response = projectService.getProjectById(projectId);

      return ResponseEntity.ok(
          ApiResponse.<ProjectResponse>builder()
              .success(true)
              .message("Project retrieved successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error retrieving project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjectsByClient() {

    try {
      List<ProjectResponse> responses = projectService.getProjectsByClient();

      return ResponseEntity.ok(
          ApiResponse.<List<ProjectResponse>>builder()
              .success(true)
              .message("Projects retrieved successfully")
              .data(responses)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error retrieving projects for authenticated client", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<List<ProjectResponse>>builder()
                  .success(false)
                  .message("An error occurred while retrieving projects")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @DeleteMapping("/{projectId}")
  public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long projectId) {

    try {
      projectService.deleteProject(projectId);

      return ResponseEntity.ok(
          ApiResponse.<Void>builder()
              .success(true)
              .message("Project deleted successfully")
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error deleting project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PutMapping("/{projectId}/validate")
  public ResponseEntity<ApiResponse<ProjectResponse>> updateProjectValidation(
      @PathVariable Long projectId, @Valid @RequestBody UpdateValidationRequest request) {

    try {
      ProjectResponse response =
          projectService.updateProjectValidation(
              projectId, request.getIsValid(), request.getValidationNotes());

      return ResponseEntity.ok(
          ApiResponse.<ProjectResponse>builder()
              .success(true)
              .message("Project validation status updated successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (ResourceNotFoundException e) {
      log.error("Project not found: {}", projectId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (Exception e) {
      log.error("Error updating project validation status {}", projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message("An error occurred while updating project validation status")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @GetMapping("/public-preview")
  public ResponseEntity<ApiResponse<List<ProjectPublicPreviewResponse>>>
      getPublicProjectPreviews() {
    try {
      List<ProjectPublicPreviewResponse> previews = projectService.getPublicProjectPreviews();
      return ResponseEntity.ok(
          ApiResponse.<List<ProjectPublicPreviewResponse>>builder()
              .success(true)
              .data(previews)
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (Exception e) {
      log.error("Error retrieving public project previews", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<List<ProjectPublicPreviewResponse>>builder()
                  .success(false)
                  .message("An error occurred while retrieving projects")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @GetMapping("/all")
  public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects() {

    try {
      List<ProjectResponse> responses = projectService.getAllProjects();

      return ResponseEntity.ok(
          ApiResponse.<List<ProjectResponse>>builder()
              .success(true)
              .message("All projects retrieved successfully")
              .data(responses)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error retrieving all projects", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<List<ProjectResponse>>builder()
                  .success(false)
                  .message("An error occurred while retrieving projects")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @GetMapping("/open")
  public ResponseEntity<ApiResponse<List<ProjectResponse>>> getOpenProjects(
      @RequestParam(value = "sortBy", required = false, defaultValue = "createdAt") String sortBy,
      @RequestParam(value = "sortDirection", required = false, defaultValue = "desc")
          String sortDirection,
      @RequestParam(value = "excludeOwnProjects", required = false, defaultValue = "false")
          boolean excludeOwnProjects) {

    try {
      List<ProjectResponse> responses =
          projectService.getOpenProjects(sortBy, sortDirection, excludeOwnProjects);

      return ResponseEntity.ok(
          ApiResponse.<List<ProjectResponse>>builder()
              .success(true)
              .message("Open projects retrieved successfully")
              .data(responses)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error retrieving open projects", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<List<ProjectResponse>>builder()
                  .success(false)
                  .message("An error occurred while retrieving open projects")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @GetMapping("/{projectId}/for-architect")
  public ResponseEntity<ApiResponse<ProjectResponse>> getProjectForArchitect(
      @PathVariable Long projectId) {

    try {
      ProjectResponse response = projectService.getProjectForArchitect(projectId);

      return ResponseEntity.ok(
          ApiResponse.<ProjectResponse>builder()
              .success(true)
              .message("Project details retrieved successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (ResourceNotFoundException e) {
      log.error("Project not found: {}", projectId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (org.springframework.security.access.AccessDeniedException e) {
      log.error("Project not open for bidding: {}", projectId, e);
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/{projectId}/confirm-negotiation")
  public ResponseEntity<ApiResponse<ProjectResponse>> confirmNegotiation(
      @PathVariable Long projectId) {

    try {
      ProjectResponse response = projectService.confirmNegotiation(projectId);

      return ResponseEntity.ok(
          ApiResponse.<ProjectResponse>builder()
              .success(true)
              .message("Negotiation confirmed, project is now in progress")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error confirming negotiation for project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/{projectId}/architect-confirm-negotiation")
  public ResponseEntity<ApiResponse<ProjectResponse>> architectConfirmNegotiation(
      @PathVariable Long projectId) {

    try {
      ProjectResponse response = projectService.architectConfirmNegotiation(projectId);

      return ResponseEntity.ok(
          ApiResponse.<ProjectResponse>builder()
              .success(true)
              .message("Negotiation confirmed by architect")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (org.springframework.security.access.AccessDeniedException e) {
      log.error("Architect not authorized for project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (Exception e) {
      log.error("Error confirming negotiation as architect for project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/{projectId}/reject-negotiation")
  public ResponseEntity<ApiResponse<ProjectResponse>> rejectNegotiation(
      @PathVariable Long projectId) {

    try {
      ProjectResponse response = projectService.rejectNegotiation(projectId);

      return ResponseEntity.ok(
          ApiResponse.<ProjectResponse>builder()
              .success(true)
              .message("Negotiation rejected, project reopened for bidding")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error rejecting negotiation for project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/{projectId}/initialize-phases")
  public ResponseEntity<ApiResponse<Void>> initializePhases(@PathVariable Long projectId) {
    try {
      projectService.initializePhasesForProject(projectId);
      return ResponseEntity.ok(
          ApiResponse.<Void>builder()
              .success(true)
              .message("Phases initialized")
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (Exception e) {
      log.error("Error initializing phases for project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @GetMapping("/{projectId}/bids")
  public ResponseEntity<ApiResponse<List<BidResponse>>> getProjectBids(
      @PathVariable Long projectId) {

    try {
      List<BidResponse> responses = projectService.getProjectBids(projectId);

      return ResponseEntity.ok(
          ApiResponse.<List<BidResponse>>builder()
              .success(true)
              .message("Project bids retrieved successfully")
              .data(responses)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error retrieving bids for project {}", projectId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<List<BidResponse>>builder()
                  .success(false)
                  .message("An error occurred while retrieving project bids")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }
}
