package com.rumantra.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.architect.domain.Porto;
import com.rumantra.architect.repository.PortoRepository;
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
import com.rumantra.ledger.service.StatusTransitionService;
import com.rumantra.notification.event.ProjectValidatedEvent;
import com.rumantra.project.domain.ProjectPhase;
import com.rumantra.project.repository.ProjectPhaseRepository;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.constants.ProjectTaxonomy;
import com.rumantra.shared.domain.ActorType;
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
  private final PortoRepository portoRepository;
  private final StatusTransitionService statusTransitionService;

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
  public ProjectResponse createDraftProject(CreateProjectRequest request) {

    Long clientId = getCurrentUserClientId();

    Client client =
        clientRepository
            .findById(clientId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Client not found with id: " + clientId));

    Project project = Project.builder().client(client).status(ProjectStatus.DRAFT).build();

    applyDraftFields(project, request);

    project = projectRepository.save(project);

    return mapToProjectResponse(project);
  }

  @Transactional
  public ProjectResponse updateDraftProject(Long projectId, CreateProjectRequest request) {
    verifyProjectOwnership(projectId);

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    if (project.getStatus() != ProjectStatus.DRAFT) {
      throw new IllegalStateException(
          "PROJECT_NOT_DRAFT: This project has already been submitted and can no longer be"
              + " edited as a draft.");
    }

    applyDraftFields(project, request);

    project = projectRepository.save(project);

    return mapToProjectResponse(project);
  }

  @Transactional
  public ProjectResponse submitProject(Long projectId, List<MultipartFile> files) {
    verifyProjectOwnership(projectId);

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    if (project.getStatus() != ProjectStatus.DRAFT) {
      throw new IllegalStateException(
          "PROJECT_NOT_DRAFT: This project has already been submitted.");
    }

    validateClientProfile(project.getClient());
    validateRequiredFieldsForSubmission(project);

    if (project.getDesignBudgetMin() != null && project.getDesignBudgetMax() != null) {
      if (project.getDesignBudgetMax() < project.getDesignBudgetMin()) {
        throw new IllegalArgumentException(
            "Maximum design budget must be greater than minimum design budget");
      }
    }

    project =
        statusTransitionService.transitionProject(
            project,
            ProjectStatus.PENDING_APPROVAL,
            statusTransitionService.actorRef(SecurityUtils.getCurrentUserId()),
            ActorType.CLIENT,
            "PROJECT_SUBMITTED",
            null);

    if (files != null && !files.isEmpty()) {
      addFilesToProject(project, files);
    }

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

  private void applyDraftFields(Project project, CreateProjectRequest request) {
    project.setTitle(request.getTitle());
    project.setLocation(request.getLocation());
    project.setFullAddress(request.getFullAddress());
    project.setCity(request.getCity());
    project.setProvince(request.getProvince());
    project.setLatitude(request.getLatitude());
    project.setLongitude(request.getLongitude());
    project.setDesignBudgetMin(request.getDesignBudgetMin());
    project.setDesignBudgetMax(request.getDesignBudgetMax());
    project.setProjectCategory(request.getProjectCategory());
    project.setBuildingFunction(request.getBuildingFunction());
    project.setProjectScope(request.getProjectScope());
    project.setSubCategory(request.getSubCategory());
    project.setLotSize(request.getLotSize());
    project.setEstimatedBuildArea(request.getEstimatedBuildArea());
    project.setNumberOfFloors(request.getNumberOfFloors());
    project.setOwnsLand(request.getOwnsLand());
    project.setHasLegalDocuments(request.getHasLegalDocuments());
    project.setScopeOfWork(request.getScopeOfWork());
    project.setDeliverables(request.getDeliverables());
    project.setDesignPreferences(request.getDesignPreferences());
    project.setContactPerson(request.getContactPerson());
    project.setExpectedStartDate(request.getExpectedStartDate());
    project.setStartDateType(
        request.getStartDateType() != null
            ? request.getStartDateType()
            : com.rumantra.client.domain.StartDateType.IMMEDIATELY);
    project.setBiddingDeadline(
        request.getBiddingDeadline() != null
            ? request.getBiddingDeadline().atTime(23, 59, 59)
            : null);
  }

  private void validateClientProfile(Client client) {
    boolean missingPhone = client.getPhoneNumber() == null || client.getPhoneNumber().isBlank();
    if (missingPhone) {
      throw new IllegalStateException(
          "PROFILE_INCOMPLETE: Please add your phone number to your profile so architects and"
              + " our team can reach you, before posting a project.");
    }
  }

  private void validateRequiredFieldsForSubmission(Project project) {
    List<String> missing = new ArrayList<>();
    if (project.getTitle() == null || project.getTitle().isBlank()) {
      missing.add("Title");
    }
    if (project.getLocation() == null || project.getLocation().isBlank()) {
      missing.add("Location");
    }
    if (project.getLotSize() == null) {
      missing.add("Lot Size");
    }
    if (project.getNumberOfFloors() == null) {
      missing.add("Number of Floors");
    }
    if (!ProjectTaxonomy.isValidScope(project.getProjectScope())) {
      missing.add("Project Scope");
    }
    String category = project.getBuildingFunction();
    if (!ProjectTaxonomy.isValidCategory(category)) {
      missing.add("Category");
    } else if (ProjectTaxonomy.requiresSubCategory(category)
        && !ProjectTaxonomy.isValidSubCategory(category, project.getSubCategory())) {
      missing.add("Sub-Category");
    }
    if (project.getScopeOfWork() == null || project.getScopeOfWork().isBlank()) {
      missing.add("Detailed Requirements");
    }
    if (project.getDesignBudgetMin() == null || project.getDesignBudgetMax() == null) {
      missing.add("Design Budget");
    }

    if (!missing.isEmpty()) {
      throw new IllegalArgumentException(
          "Please complete the following before posting: " + String.join(", ", missing));
    }
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

    // Soft delete: the project's status ledger is append-only and references this row, so the
    // record cannot be removed. Blobs are still purged above.
    Long userId = SecurityUtils.getCurrentUserId();
    statusTransitionService.transitionProject(
        project,
        ProjectStatus.DELETED,
        statusTransitionService.actorRef(userId),
        ActorType.CLIENT,
        "PROJECT_DELETED",
        null);
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

    project.setBiddingDeadline(isValid ? java.time.LocalDateTime.now().plusWeeks(2) : null);
    project.setValidationNotes(validationNotes);
    project =
        statusTransitionService.transitionProject(
            project,
            isValid ? ProjectStatus.OPEN : ProjectStatus.REJECTED,
            statusTransitionService.actorRef(SecurityUtils.getCurrentUserId()),
            ActorType.SUPERUSER,
            isValid ? "PROJECT_VALIDATED" : "PROJECT_REJECTED",
            validationNotes == null ? null : Map.of("validationNotes", validationNotes));

    log.info("Project {} validation status updated to {} by superuser", projectId, isValid);

    Long superuserId = SecurityUtils.getCurrentUserId();
    String projectTitle = buildProjectTitle(project);

    eventPublisher.publishEvent(
        new ProjectValidatedEvent(
            this,
            project.getId(),
            project.getClient().getUser().getId(),
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
    List<Project> projects = projectRepository.findAllNotDeleted();
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

    // Open projects are browsable by any architect so they can decide whether to bid.
    // Every other status is history: only architects who actually bid may look back at it,
    // whatever the outcome of their bid was.
    boolean hasBid = bidRepository.existsByProjectIdAndArchitectUserId(projectId, userId);
    if (project.getStatus() != ProjectStatus.OPEN && !hasBid) {
      throw new org.springframework.security.access.AccessDeniedException(
          "This project is not accessible");
    }

    ProjectResponse response = mapToProjectResponse(project);

    if (project.getStatus() == ProjectStatus.COMPLETED) {
      List<Bid> acceptedBids =
          bidRepository.findByProjectIdAndStatus(projectId, BidStatus.ACCEPTED);
      boolean isWinningArchitect =
          acceptedBids.stream().anyMatch(b -> b.getArchitect().getUser().getId().equals(userId));
      // The archived porto belongs to the winning architect, so only they get the link
      if (isWinningArchitect) {
        response.setArchivedPortoId(
            portoRepository.findBySourceProjectId(projectId).map(Porto::getId).orElse(null));
      }
    }

    return response;
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
      project =
          statusTransitionService.transitionProject(
              project,
              ProjectStatus.IN_PROGRESS,
              statusTransitionService.actorRef(SecurityUtils.getCurrentUserId()),
              ActorType.CLIENT,
              "NEGOTIATION_CONFIRMED",
              null);
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
      project =
          statusTransitionService.transitionProject(
              project,
              ProjectStatus.IN_PROGRESS,
              statusTransitionService.actorRef(SecurityUtils.getCurrentUserId()),
              ActorType.ARCHITECT,
              "NEGOTIATION_CONFIRMED",
              null);
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

    project =
        statusTransitionService.transitionProject(
            project,
            ProjectStatus.OPEN,
            statusTransitionService.actorRef(SecurityUtils.getCurrentUserId()),
            ActorType.CLIENT,
            "NEGOTIATION_REJECTED",
            null);

    return mapToProjectResponse(project);
  }

  private List<ProjectFile> addFilesToProject(Project project, List<MultipartFile> files) {
    List<ProjectFile> added = new ArrayList<>();
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
      added.add(projectFile);
    }
    return added;
  }

  @Transactional
  public List<ProjectFileDto> uploadDraftFiles(Long projectId, List<MultipartFile> files) {
    verifyProjectOwnership(projectId);

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    if (project.getStatus() != ProjectStatus.DRAFT) {
      throw new IllegalStateException(
          "PROJECT_NOT_DRAFT: Images can only be uploaded while the project is still a draft.");
    }

    List<ProjectFile> added = addFilesToProject(project, files);

    return added.stream().map(this::mapToProjectFileDto).collect(Collectors.toList());
  }

  @Transactional
  public void deleteProjectFile(Long projectId, Long fileId) {
    verifyProjectOwnership(projectId);

    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Project not found with id: " + projectId));

    if (project.getStatus() != ProjectStatus.DRAFT) {
      throw new IllegalStateException(
          "PROJECT_NOT_DRAFT: Images can only be removed while the project is still a draft.");
    }

    ProjectFile file =
        projectFileRepository
            .findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + fileId));

    if (!file.getProject().getId().equals(projectId)) {
      throw new IllegalArgumentException("File does not belong to this project");
    }

    fileStorageService.deleteImages(List.of(file.getFilePath()));
    projectFileRepository.delete(file);
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
        .fullAddress(project.getFullAddress())
        .city(project.getCity())
        .province(project.getProvince())
        .latitude(project.getLatitude())
        .longitude(project.getLongitude())
        .designBudgetMin(project.getDesignBudgetMin())
        .designBudgetMax(project.getDesignBudgetMax())
        .projectCategory(project.getProjectCategory())
        .buildingFunction(project.getBuildingFunction())
        .projectScope(project.getProjectScope())
        .subCategory(project.getSubCategory())
        .lotSize(project.getLotSize())
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

  @Transactional(readOnly = true)
  public List<com.rumantra.client.dto.ProjectPublicPreviewResponse> getPublicProjectPreviews() {
    List<ProjectStatus> publicStatuses =
        java.util.Arrays.asList(
            ProjectStatus.OPEN, ProjectStatus.IN_PROGRESS, ProjectStatus.COMPLETED);

    return projectRepository.findPublicProjects(publicStatuses).stream()
        .limit(20)
        .map(this::mapToPublicPreview)
        .collect(Collectors.toList());
  }

  private com.rumantra.client.dto.ProjectPublicPreviewResponse mapToPublicPreview(Project project) {
    String firstImageUrl =
        project.getFiles().stream()
            .filter(f -> f.getFileType() != null && f.getFileType().startsWith("image/"))
            .findFirst()
            .map(f -> fileStorageService.getPublicUrl(f.getFilePath()))
            .orElse(null);

    return com.rumantra.client.dto.ProjectPublicPreviewResponse.builder()
        .id(project.getId())
        .title(project.getTitle())
        .location(project.getLocation())
        // Deliberately no fullAddress/latitude/longitude - this DTO is served unauthenticated.
        .city(project.getCity())
        .province(project.getProvince())
        .budgetDisplay(project.getDesignBudgetMax())
        .projectCategory(project.getProjectCategory())
        .buildingFunction(project.getBuildingFunction())
        .projectScope(project.getProjectScope())
        .subCategory(project.getSubCategory())
        .status(project.getStatus())
        .firstImageUrl(firstImageUrl)
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

    statusTransitionService.transitionProject(
        project, ProjectStatus.BIDDING_CLOSED, null, ActorType.SYSTEM, "BIDDING_CLOSED", null);

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
    } else if (project.getSubCategory() != null && !project.getSubCategory().isBlank()) {
      return humanizeToken(project.getSubCategory()) + " (Project #" + project.getId() + ")";
    } else if (project.getBuildingFunction() != null && !project.getBuildingFunction().isBlank()) {
      return humanizeToken(project.getBuildingFunction()) + " (Project #" + project.getId() + ")";
    } else if (project.getProjectCategory() != null && !project.getProjectCategory().isBlank()) {
      return project.getProjectCategory() + " (Project #" + project.getId() + ")";
    } else {
      return "Project #" + project.getId();
    }
  }

  /** Taxonomy tokens are SCREAMING_SNAKE; never show one to a user verbatim. */
  private String humanizeToken(String token) {
    String[] words = token.toLowerCase().split("_");
    StringBuilder sb = new StringBuilder();
    for (String word : words) {
      if (word.isEmpty()) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
    }
    return sb.toString();
  }
}
