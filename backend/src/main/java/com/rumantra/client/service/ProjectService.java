package com.rumantra.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidPaymentPhase;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.dto.BidResponse;
import com.rumantra.bidding.repository.BidPaymentPhaseRepository;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.bidding.service.BidService;
import com.rumantra.chat.repository.ConversationRepository;
import com.rumantra.chat.service.ConversationService;
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
import com.rumantra.project.domain.ProjectPhase;
import com.rumantra.project.repository.ProjectPhaseRepository;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.shared.storage.FileStorageService;

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
  private final BidService bidService;
  private final BidRepository bidRepository;
  private final FileStorageService fileStorageService;
  private final ConversationRepository conversationRepository;
  private final ConversationService conversationService;
  private final BidPaymentPhaseRepository bidPaymentPhaseRepository;
  private final ProjectPhaseRepository projectPhaseRepository;

  @PersistenceContext private EntityManager entityManager;

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

    Long clientId = getCurrentUserClientId();

    Client client =
        clientRepository
            .findById(clientId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Client not found with id: " + clientId));

    if (request.getDesignBudgetMin() != null && request.getDesignBudgetMax() != null) {
      if (request.getDesignBudgetMax() < request.getDesignBudgetMin()) {
        throw new IllegalArgumentException(
            "Maximum design budget must be greater than minimum design budget");
      }
    }

    Project project =
        Project.builder()
            .client(client)
            .title(request.getTitle())
            .location(request.getLocation())
            .budgetTotal(request.getBudgetTotal())
            .designBudgetMin(request.getDesignBudgetMin())
            .designBudgetMax(request.getDesignBudgetMax())
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
            .startDateType(
                request.getStartDateType() != null
                    ? request.getStartDateType()
                    : com.rumantra.client.domain.StartDateType.IMMEDIATELY)
            .biddingDeadline(
                request.getBiddingDeadline() != null
                    ? request.getBiddingDeadline().atTime(23, 59, 59)
                    : null)
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

    List<String> fileUrls =
        project.getFiles().stream().map(ProjectFile::getFilePath).collect(Collectors.toList());
    if (!fileUrls.isEmpty()) {
      fileStorageService.deleteImages(fileUrls);
    }

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

    if (isValid) {
      project.setStatus(ProjectStatus.OPEN);
      project.setBiddingDeadline(java.time.LocalDateTime.now().plusWeeks(2));
    } else {
      project.setStatus(ProjectStatus.REJECTED);
      project.setBiddingDeadline(null);
    }
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
   * @param excludeOwnProjects If true, filter out projects created by the current user
   * @return List of OPEN projects
   */
  @Transactional(readOnly = true)
  public List<ProjectResponse> getOpenProjects(
      String sortBy, String sortDirection, boolean excludeOwnProjects) {
    Sort.Direction direction =
        "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");

    List<Project> projects = projectRepository.findByStatusWithFiles(ProjectStatus.OPEN, sort);

    if (excludeOwnProjects) {
      Long currentUserId = SecurityUtils.getCurrentUserId();
      projects =
          projects.stream()
              .filter(project -> !project.getClient().getUser().getId().equals(currentUserId))
              .collect(Collectors.toList());
    }

    return projects.stream().map(this::mapToProjectResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public ProjectResponse getProjectForArchitect(Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();

    Project project =
        projectRepository
            .findByIdWithFiles(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    if (project.getStatus() == ProjectStatus.OPEN
        || project.getStatus() == ProjectStatus.NEGOTIATION) {
      return mapToProjectResponse(project);
    }

    if (project.getStatus() == ProjectStatus.IN_PROGRESS
        || project.getStatus() == ProjectStatus.COMPLETED) {
      List<Bid> acceptedBids =
          bidRepository.findByProjectIdAndStatus(projectId, BidStatus.ACCEPTED);
      boolean isWinningArchitect =
          acceptedBids.stream().anyMatch(b -> b.getArchitect().getUser().getId().equals(userId));
      if (isWinningArchitect) {
        return mapToProjectResponse(project);
      }
    }

    throw new org.springframework.security.access.AccessDeniedException(
        "This project is not accessible");
  }

  @Transactional
  public ProjectResponse confirmNegotiation(Long projectId) {
    verifyProjectOwnership(projectId);

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    if (project.getStatus() != ProjectStatus.NEGOTIATION) {
      throw new RuntimeException(
          "Project is not in negotiation phase. Current status: " + project.getStatus());
    }

    project.setClientConfirmedAt(java.time.LocalDateTime.now());
    if (project.getArchitectConfirmedAt() != null) {
      project.setStatus(ProjectStatus.IN_PROGRESS);
      project = projectRepository.save(project);
      initializeProjectPhasesFromBid(project);
    } else {
      project = projectRepository.save(project);
    }

    return mapToProjectResponse(project);
  }

  @Transactional
  public ProjectResponse architectConfirmNegotiation(Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    if (project.getStatus() != ProjectStatus.NEGOTIATION) {
      throw new RuntimeException(
          "Project is not in negotiation phase. Current status: " + project.getStatus());
    }

    List<Bid> acceptedBids = bidRepository.findByProjectIdAndStatus(projectId, BidStatus.ACCEPTED);
    boolean isArchitectForProject =
        acceptedBids.stream().anyMatch(b -> b.getArchitect().getUser().getId().equals(userId));
    if (!isArchitectForProject) {
      throw new org.springframework.security.access.AccessDeniedException(
          "You are not the architect for this project");
    }

    project.setArchitectConfirmedAt(java.time.LocalDateTime.now());
    if (project.getClientConfirmedAt() != null) {
      project.setStatus(ProjectStatus.IN_PROGRESS);
      project = projectRepository.save(project);
      initializeProjectPhasesFromBid(project);
    } else {
      project = projectRepository.save(project);
    }

    return mapToProjectResponse(project);
  }

  @Transactional
  public void initializePhasesForProject(Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    if (!project.getClient().getUser().getId().equals(userId)) {
      throw new org.springframework.security.access.AccessDeniedException(
          "You are not the client for this project");
    }
    if (project.getStatus() != ProjectStatus.IN_PROGRESS) {
      throw new RuntimeException("Project is not IN_PROGRESS");
    }
    initializeProjectPhasesFromBid(project);
  }

  private void initializeProjectPhasesFromBid(Project project) {
    List<ProjectPhase> existingPhases =
        projectPhaseRepository.findByProjectIdOrderByPhaseNumberAsc(project.getId());
    if (!existingPhases.isEmpty()) {
      return;
    }
    List<Bid> acceptedBids =
        bidRepository.findByProjectIdAndStatus(project.getId(), BidStatus.ACCEPTED);
    if (acceptedBids.isEmpty()) {
      return;
    }
    Bid acceptedBid = acceptedBids.get(0);
    List<BidPaymentPhase> bidPhases =
        bidPaymentPhaseRepository.findByBidIdOrderByPhaseNumber(acceptedBid.getId());

    List<ProjectPhase> phases = new ArrayList<>();
    for (BidPaymentPhase bidPhase : bidPhases) {
      phases.add(
          ProjectPhase.builder()
              .project(project)
              .phaseNumber(bidPhase.getPhaseNumber())
              .title(
                  bidPhase.getTitle() != null
                      ? bidPhase.getTitle()
                      : "Phase " + bidPhase.getPhaseNumber())
              .description(
                  bidPhase.getDeliverables() != null
                      ? String.join(", ", bidPhase.getDeliverables())
                      : null)
              .amount(bidPhase.getAmount())
              .maxRevisions(
                  bidPhase.getRevisionRounds() != null && bidPhase.getRevisionRounds() > 0
                      ? bidPhase.getRevisionRounds()
                      : 3)
              .build());
    }
    projectPhaseRepository.saveAll(phases);
    log.info("Initialized {} project phases for project {}", phases.size(), project.getId());
  }

  @Transactional
  public ProjectResponse rejectNegotiation(Long projectId) {
    verifyProjectOwnership(projectId);

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    if (project.getStatus() != ProjectStatus.NEGOTIATION) {
      throw new RuntimeException(
          "Project is not in negotiation phase. Current status: " + project.getStatus());
    }

    List<Bid> acceptedBids = bidRepository.findByProjectIdAndStatus(projectId, BidStatus.ACCEPTED);
    if (acceptedBids.isEmpty()) {
      throw new RuntimeException("No accepted bid found for this project");
    }

    Bid acceptedBid = acceptedBids.get(0);
    bidService.refundBid(acceptedBid, "Negotiation rejected by client");

    conversationRepository
        .findProjectConversationByBidId(acceptedBid.getId())
        .ifPresent(
            conversation -> {
              conversationService.archiveConversation(conversation.getId());
            });

    project.setStatus(ProjectStatus.OPEN);
    project = projectRepository.save(project);

    return mapToProjectResponse(project);
  }

  private void addFilesToProject(Project project, List<MultipartFile> files) {
    for (MultipartFile file : files) {
      if (file.isEmpty()) {
        continue;
      }

      String contentType = file.getContentType();
      if (contentType == null
          || (!contentType.equals("image/png")
              && !contentType.equals("image/jpeg")
              && !contentType.equals("application/pdf"))) {
        log.warn("Skipping file with unsupported type: {}", contentType);
        continue;
      }

      String storedPath;
      if (contentType.startsWith("image/")) {
        storedPath = fileStorageService.uploadImage(file, "projects");
      } else {
        storedPath = fileStorageService.uploadFile(file, "projects");
      }

      ProjectFile projectFile =
          ProjectFile.builder()
              .project(project)
              .fileName(file.getOriginalFilename())
              .filePath(storedPath)
              .fileType(contentType)
              .fileSize(file.getSize())
              .build();

      project.getFiles().add(projectFile);
      projectFileRepository.save(projectFile);
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
        .title(project.getTitle())
        .location(project.getLocation())
        .budgetTotal(project.getBudgetTotal())
        .designBudgetMin(project.getDesignBudgetMin())
        .designBudgetMax(project.getDesignBudgetMax())
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
        .startDateType(project.getStartDateType())
        .status(project.getStatus())
        .isValid(
            project.getStatus() != ProjectStatus.PENDING_APPROVAL
                    && project.getStatus() != ProjectStatus.REJECTED
                ? Boolean.TRUE
                : Boolean.FALSE)
        .validationNotes(project.getValidationNotes())
        .biddingDeadline(project.getBiddingDeadline())
        .clientConfirmed(project.getClientConfirmedAt() != null)
        .architectConfirmed(project.getArchitectConfirmedAt() != null)
        .files(fileDtos)
        .bidCount(bidRepository.countByProjectId(project.getId()))
        .createdAt(project.getCreatedAt())
        .updatedAt(project.getUpdatedAt())
        .build();
  }

  private ProjectFileDto mapToProjectFileDto(ProjectFile projectFile) {
    return ProjectFileDto.builder()
        .id(projectFile.getId())
        .fileName(projectFile.getFileName())
        .filePath(fileStorageService.getPublicUrl(projectFile.getFilePath()))
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
  public List<BidResponse> getProjectBids(Long projectId) {
    verifyProjectOwnership(projectId);
    return bidService.getBidsByProject(projectId);
  }

  @Transactional
  public void closeExpiredProject(Long projectId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    if (project.getStatus() != ProjectStatus.OPEN) {
      return;
    }

    project.setStatus(ProjectStatus.BIDDING_CLOSED);
    projectRepository.save(project);

    List<Bid> pendingBids = bidRepository.findByProjectIdAndStatus(projectId, BidStatus.PENDING);
    for (Bid bid : pendingBids) {
      bidService.refundBid(bid, "Bidding period ended with no winner selected");
    }

    log.info(
        "Project {} closed for deadline expiry. {} bids refunded.", projectId, pendingBids.size());
  }

  private String buildProjectTitle(Project project) {
    if (project.getTitle() != null && !project.getTitle().isBlank()) {
      return project.getTitle();
    } else if (project.getBuildingFunction() != null && !project.getBuildingFunction().isBlank()) {
      return project.getBuildingFunction() + " (Project #" + project.getId() + ")";
    } else if (project.getProjectCategory() != null && !project.getProjectCategory().isBlank()) {
      return project.getProjectCategory() + " (Project #" + project.getId() + ")";
    } else {
      return "Project #" + project.getId();
    }
  }
}
