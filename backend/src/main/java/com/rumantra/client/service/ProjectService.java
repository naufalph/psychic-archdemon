package com.rumantra.client.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.client.domain.Client;
import com.rumantra.client.domain.Project;
import com.rumantra.client.domain.ProjectFile;
import com.rumantra.client.domain.ProjectStatus;
import com.rumantra.client.dto.CreateProjectRequest;
import com.rumantra.client.dto.ProjectFileDto;
import com.rumantra.client.dto.ProjectResponse;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.client.repository.ProjectFileRepository;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.notification.event.ProjectValidatedEvent;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.exception.ResourceNotFoundException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final ProjectFileRepository projectFileRepository;
  private final ClientRepository clientRepository;
  private final ApplicationEventPublisher eventPublisher;

  @PersistenceContext private EntityManager entityManager;

  @Value("${file.upload-dir:uploads/projects}")
  private String uploadDir;

  /**
   * Get the client ID for the currently authenticated user.
   *
   * @return The client ID
   * @throws RuntimeException if the user doesn't have a client profile
   */
  private Long getCurrentUserClientId() {
    Long userId = SecurityUtils.getCurrentUserId();

    Client client =
        clientRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Current user does not have a client profile. Please activate the client role first."));

    return client.getId();
  }

  /**
   * Verify that the currently authenticated user owns the specified project.
   *
   * @param projectId The project ID to verify ownership for
   * @throws RuntimeException if project doesn't exist or user doesn't own it
   */
  private void verifyProjectOwnership(Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    if (!project.getClient().getUser().getId().equals(userId)) {
      log.warn(
          "Access denied: User {} attempted to access project {} resources", userId, projectId);
      throw new RuntimeException("You do not have permission to access this project's resources");
    }

    log.debug("Ownership verified: User {} owns project {}", userId, projectId);
  }

  @Transactional
  public ProjectResponse createProject(CreateProjectRequest request, List<MultipartFile> files) {

    // Get current user's client ID
    Long clientId = getCurrentUserClientId();

    // Get client entity
    Client client =
        clientRepository
            .findById(clientId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Client not found with id: " + clientId));

    // Validate budget range
    if (request.getBudgetMax() < request.getBudgetMin()) {
      throw new IllegalArgumentException("Maximum budget must be greater than minimum budget");
    }

    // Build project entity
    Project project =
        Project.builder()
            .client(client)
            .budgetMin(request.getBudgetMin())
            .budgetMax(request.getBudgetMax())
            .projectCategory(request.getProjectCategory())
            .buildingFunction(request.getBuildingFunction())
            .estimatedBuildArea(request.getEstimatedBuildArea())
            .numberOfFloors(request.getNumberOfFloors())
            .ownsLand(request.getOwnsLand())
            .hasLegalDocuments(request.getHasLegalDocuments())
            .scopeOfWork(request.getScopeOfWork())
            .deliverables(request.getDeliverables())
            .designPreferences(request.getDesignPreferences())
            .contactPerson(request.getContactPerson())
            .expectedStartDate(request.getExpectedStartDate())
            .build(); // status defaults to PENDING_APPROVAL

    project = projectRepository.save(project);

    // Handle file uploads if present
    if (files != null && !files.isEmpty()) {
      addFilesToProject(project, files);
    }

    // Reload with files
    Project finalProject = project;
    project =
        projectRepository
            .findByIdWithFiles(project.getId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Project not found with id: " + finalProject.getId()));

    return mapToProjectResponse(project);
  }

  @Transactional(readOnly = true)
  public ProjectResponse getProjectById(Long projectId) {
    // Verify ownership before retrieving
    verifyProjectOwnership(projectId);

    Project project =
        projectRepository
            .findByIdWithFiles(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));
    return mapToProjectResponse(project);
  }

  @Transactional(readOnly = true)
  public List<ProjectResponse> getProjectsByClient() {
    // Get current user's client ID
    Long clientId = getCurrentUserClientId();

    List<Project> projects = projectRepository.findByClientIdWithFiles(clientId);
    return projects.stream().map(this::mapToProjectResponse).collect(Collectors.toList());
  }

  @Transactional
  public void deleteProject(Long projectId) {
    // Verify ownership before deleting
    verifyProjectOwnership(projectId);

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    // Delete associated files from disk
    project.getFiles().forEach(this::deleteFileFromDisk);

    projectRepository.delete(project);
  }

  /**
   * Update project validation status (Superuser only).
   *
   * @param projectId The project ID to update
   * @param isValid The new validation status
   * @return Updated project response
   */
  @Transactional
  public ProjectResponse updateProjectValidation(
      Long projectId, Boolean isValid, String validationNotes) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    // Map boolean to ProjectStatus
    project.setStatus(isValid ? ProjectStatus.OPEN : ProjectStatus.REJECTED);
    project.setValidationNotes(validationNotes);
    project = projectRepository.save(project);

    log.info("Project {} validation status updated to {} by superuser", projectId, isValid);

    Long superuserId = SecurityUtils.getCurrentUserId();
    String projectTitle = buildProjectTitle(project);

    eventPublisher.publishEvent(
        new ProjectValidatedEvent(
            this,
            project.getId(),
            project.getClient().getId(),
            projectTitle,
            isValid,
            superuserId,
            validationNotes));

    log.info("ProjectValidatedEvent published for project {}", projectId);

    return mapToProjectResponse(project);
  }

  /**
   * Get all projects (Superuser only).
   *
   * @return List of all projects regardless of validation status
   */
  @Transactional(readOnly = true)
  public List<ProjectResponse> getAllProjects() {
    List<Project> projects = projectRepository.findAll();
    return projects.stream().map(this::mapToProjectResponse).collect(Collectors.toList());
  }

  /**
   * Get all OPEN projects for architects (Architect role required).
   *
   * @param sortBy The field to sort by (e.g., "createdAt", "budgetMax", "budgetMin")
   * @param sortDirection The sort direction ("asc" or "desc")
   * @return List of OPEN projects
   */
  @Transactional(readOnly = true)
  public List<ProjectResponse> getOpenProjects(String sortBy, String sortDirection) {
    Sort.Direction direction =
        "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");

    List<Project> projects = projectRepository.findByStatusWithFiles(ProjectStatus.OPEN, sort);
    return projects.stream().map(this::mapToProjectResponse).collect(Collectors.toList());
  }

  private void addFilesToProject(Project project, List<MultipartFile> files) {
    try {
      // Create upload directory if it doesn't exist
      Path uploadPath = Paths.get(uploadDir);
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      for (MultipartFile file : files) {
        if (file.isEmpty()) {
          continue;
        }

        // Validate file type (png, jpg, pdf)
        String contentType = file.getContentType();
        if (contentType == null
            || (!contentType.equals("image/png")
                && !contentType.equals("image/jpeg")
                && !contentType.equals("application/pdf"))) {
          log.warn("Skipping file with unsupported type: {}", contentType);
          continue;
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension =
            originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String uniqueFilename = UUID.randomUUID() + fileExtension;

        // Save file to disk
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create ProjectFile entity
        ProjectFile projectFile =
            ProjectFile.builder()
                .project(project)
                .fileName(originalFilename)
                .filePath(filePath.toString())
                .fileType(contentType)
                .fileSize(file.getSize())
                .build();

        project.getFiles().add(projectFile);
        projectFileRepository.save(projectFile);
      }
    } catch (IOException e) {
      log.error("Error uploading files for project {}", project.getId(), e);
      throw new RuntimeException("Failed to upload files", e);
    }
  }

  private void deleteFileFromDisk(ProjectFile projectFile) {
    try {
      Path filePath = Paths.get(projectFile.getFilePath());
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      log.error("Error deleting file: {}", projectFile.getFilePath(), e);
    }
  }

  private ProjectResponse mapToProjectResponse(Project project) {
    List<ProjectFileDto> fileDtos =
        project.getFiles() != null
            ? project.getFiles().stream()
                .map(this::mapToProjectFileDto)
                .collect(Collectors.toList())
            : new ArrayList<>();

    return ProjectResponse.builder()
        .id(project.getId())
        .clientId(project.getClient().getId())
        .budgetMin(project.getBudgetMin())
        .budgetMax(project.getBudgetMax())
        .projectCategory(project.getProjectCategory())
        .buildingFunction(project.getBuildingFunction())
        .estimatedBuildArea(project.getEstimatedBuildArea())
        .numberOfFloors(project.getNumberOfFloors())
        .ownsLand(project.getOwnsLand())
        .hasLegalDocuments(project.getHasLegalDocuments())
        .scopeOfWork(project.getScopeOfWork())
        .deliverables(project.getDeliverables())
        .designPreferences(project.getDesignPreferences())
        .contactPerson(project.getContactPerson())
        .expectedStartDate(project.getExpectedStartDate())
        .status(project.getStatus())
        .isValid(
            project.getStatus() == ProjectStatus.OPEN
                ? true
                : (project.getStatus() == ProjectStatus.REJECTED ? false : null))
        .validationNotes(project.getValidationNotes())
        .files(fileDtos)
        .createdAt(project.getCreatedAt())
        .updatedAt(project.getUpdatedAt())
        .build();
  }

  private ProjectFileDto mapToProjectFileDto(ProjectFile projectFile) {
    return ProjectFileDto.builder()
        .id(projectFile.getId())
        .fileName(projectFile.getFileName())
        .filePath(projectFile.getFilePath())
        .fileType(projectFile.getFileType())
        .fileSize(projectFile.getFileSize())
        .uploadedAt(projectFile.getUploadedAt())
        .build();
  }

  /**
   * Build a human-readable project title for notifications.
   *
   * @param project The project entity
   * @return A descriptive title
   */
  private String buildProjectTitle(Project project) {
    if (project.getBuildingFunction() != null && !project.getBuildingFunction().isBlank()) {
      return project.getBuildingFunction() + " (Project #" + project.getId() + ")";
    } else if (project.getProjectCategory() != null && !project.getProjectCategory().isBlank()) {
      return project.getProjectCategory() + " (Project #" + project.getId() + ")";
    } else {
      return "Project #" + project.getId();
    }
  }
}
