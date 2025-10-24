package com.rumantra.client.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.client.dto.CreateProjectRequest;
import com.rumantra.client.dto.ProjectResponse;
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
      @Valid @ModelAttribute CreateProjectRequest request,
      @RequestParam(value = "files", required = false) List<MultipartFile> files) {

    try {
      log.info("Creating project with {} files", files != null ? files.size() : 0);

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
}
