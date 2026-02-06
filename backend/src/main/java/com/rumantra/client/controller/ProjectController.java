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
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

  private final ProjectService projectService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
      @Valid @RequestPart("project") CreateProjectRequest request,
      @RequestParam(value = "files", required = false) List<MultipartFile> files) {

    try {

      ProjectResponse response = projectService.createProject(request, files);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(true)
                  .message("Project created successfully!")
                  .data(response)
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (IllegalArgumentException e) {
      log.error("Validation error creating project", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (ResourceNotFoundException e) {
      log.error("Validation error creating project", e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (Exception e) {
      log.error("Unexpected error creating project", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ProjectResponse>builder()
                  .success(false)
                  .message("An error occurred while creating project")
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
          String sortDirection) {

    try {
      List<ProjectResponse> responses = projectService.getOpenProjects(sortBy, sortDirection);

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
