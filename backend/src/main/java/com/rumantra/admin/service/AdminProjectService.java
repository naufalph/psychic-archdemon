package com.rumantra.admin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.admin.dto.AdminNegotiationResolutionRequest;
import com.rumantra.admin.dto.AdminProjectDetailResponse;
import com.rumantra.admin.dto.NegotiationDisputeResponse;
import com.rumantra.admin.event.NegotiationDisputeResolvedEvent;
import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidPaymentPhase;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidPaymentPhaseRepository;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.bidding.service.BidService;
import com.rumantra.client.domain.Client;
import com.rumantra.client.domain.Project;
import com.rumantra.client.domain.ProjectStatus;
import com.rumantra.client.dto.ProjectResponse;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.client.service.ProjectService;
import com.rumantra.ledger.service.StatusTransitionService;
import com.rumantra.project.domain.ProjectPhase;
import com.rumantra.project.repository.ProjectPhaseRepository;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.domain.ActorType;
import com.rumantra.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProjectService {

  private final ProjectService projectService;
  private final ProjectRepository projectRepository;
  private final ClientRepository clientRepository;
  private final BidRepository bidRepository;
  private final BidPaymentPhaseRepository bidPaymentPhaseRepository;
  private final ProjectPhaseRepository projectPhaseRepository;
  private final BidService bidService;
  private final ApplicationEventPublisher eventPublisher;
  private final StatusTransitionService statusTransitionService;

  @Transactional(readOnly = true)
  public List<ProjectResponse> getProjects(ProjectStatus status) {
    List<ProjectResponse> all = projectService.getAllProjects();
    if (status == null) return all;
    return all.stream().filter(p -> p.getStatus() == status).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public AdminProjectDetailResponse getProjectDetail(Long projectId) {
    ProjectResponse project =
        projectService.getAllProjects().stream()
            .filter(p -> p.getId().equals(projectId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

    Client client =
        clientRepository
            .findById(project.getClientId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Client not found: " + project.getClientId()));

    return AdminProjectDetailResponse.builder()
        .project(project)
        .clientName(
            String.join(
                    " ",
                    client.getUser().getFirstName() != null ? client.getUser().getFirstName() : "",
                    client.getUser().getLastName() != null ? client.getUser().getLastName() : "")
                .trim())
        .clientEmail(client.getUser().getEmail())
        .clientPhone(client.getPhoneNumber())
        .clientPhoneVerified(client.isPhoneNumVerified())
        .clientKtpNum(client.getKtpNum())
        .clientKtpVerified(client.isKtpVerified())
        .build();
  }

  @Transactional
  public ProjectResponse forceCancel(Long projectId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

    statusTransitionService.transitionProject(
        project,
        ProjectStatus.CANCELLED,
        statusTransitionService.actorRef(SecurityUtils.getCurrentUserId()),
        ActorType.SUPERUSER,
        "PROJECT_FORCE_CANCELLED",
        null);

    log.info("Admin force-cancelled project {}", projectId);
    return projectService.getAllProjects().stream()
        .filter(p -> p.getId().equals(projectId))
        .findFirst()
        .orElseThrow();
  }

  @Transactional
  public ProjectResponse overrideNegotiation(Long projectId) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

    if (project.getStatus() != ProjectStatus.NEGOTIATION) {
      throw new IllegalStateException(
          "Project is not in NEGOTIATION status. Current: " + project.getStatus());
    }

    project.setClientConfirmedAt(LocalDateTime.now());
    project.setArchitectConfirmedAt(LocalDateTime.now());
    project =
        statusTransitionService.transitionProject(
            project,
            ProjectStatus.IN_PROGRESS,
            statusTransitionService.actorRef(SecurityUtils.getCurrentUserId()),
            ActorType.SUPERUSER,
            "NEGOTIATION_OVERRIDDEN",
            null);

    initializeProjectPhasesFromBid(project);

    log.info("Admin overrode negotiation for project {}, moved to IN_PROGRESS", projectId);
    return projectService.getAllProjects().stream()
        .filter(p -> p.getId().equals(projectId))
        .findFirst()
        .orElseThrow();
  }

  @Transactional(readOnly = true)
  public List<NegotiationDisputeResponse> getNegotiationDisputes() {
    List<Project> projects = projectRepository.findByStatus(ProjectStatus.NEGOTIATION_EXPIRED);
    List<NegotiationDisputeResponse> result = new ArrayList<>();

    for (Project project : projects) {
      List<Bid> acceptedBids =
          bidRepository.findByProjectIdAndStatus(project.getId(), BidStatus.ACCEPTED);
      Bid acceptedBid = acceptedBids.isEmpty() ? null : acceptedBids.get(0);

      result.add(
          NegotiationDisputeResponse.builder()
              .projectId(project.getId())
              .projectTitle(project.getTitle())
              .clientName(displayName(project.getClient().getUser()))
              .clientEmail(project.getClient().getUser().getEmail())
              .architectName(
                  acceptedBid != null ? displayName(acceptedBid.getArchitect().getUser()) : null)
              .architectCompany(
                  acceptedBid != null ? acceptedBid.getArchitect().getCompanyName() : null)
              .bidAmount(acceptedBid != null ? acceptedBid.getBidAmount() : null)
              .acceptedAt(acceptedBid != null ? acceptedBid.getAcceptedAt() : null)
              .expiredAt(project.getUpdatedAt())
              .build());
    }

    return result;
  }

  @Transactional
  public ProjectResponse resolveNegotiationDispute(
      Long projectId, Long superuserUserId, AdminNegotiationResolutionRequest req) {
    Project project =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

    if (project.getStatus() != ProjectStatus.NEGOTIATION_EXPIRED) {
      throw new IllegalStateException(
          "Project is not awaiting negotiation-dispute review. Current: " + project.getStatus());
    }

    List<Bid> allBids = bidRepository.findByProjectId(projectId);
    Bid acceptedBid =
        allBids.stream().filter(b -> b.getStatus() == BidStatus.ACCEPTED).findFirst().orElse(null);

    boolean clientAbandoned =
        req.getDecision() == AdminNegotiationResolutionRequest.Decision.CLIENT_ABANDONED;
    String reason =
        clientAbandoned
            ? "Negotiation dispute resolved: client abandoned the project"
            : "Negotiation dispute resolved: architect abandoned the project";

    for (Bid bid : allBids) {
      boolean isAcceptedBid = acceptedBid != null && bid.getId().equals(acceptedBid.getId());
      boolean shouldRefund = clientAbandoned || !isAcceptedBid;
      if (shouldRefund && bid.getStatus() != BidStatus.REFUNDED) {
        bidService.refundBid(bid, reason);
      }
    }

    project =
        statusTransitionService.transitionProject(
            project,
            ProjectStatus.CANCELLED,
            statusTransitionService.actorRef(superuserUserId),
            ActorType.SUPERUSER,
            "NEGOTIATION_DISPUTE_RESOLVED",
            Map.of("reason", reason == null ? "" : reason));

    log.info(
        "Superuser {} resolved negotiation dispute for project {}: decision={}",
        superuserUserId,
        projectId,
        req.getDecision());

    if (acceptedBid != null) {
      eventPublisher.publishEvent(
          new NegotiationDisputeResolvedEvent(
              this,
              project.getId(),
              project.getTitle(),
              project.getClient().getUser().getId(),
              project.getClient().getUser().getEmail(),
              acceptedBid.getArchitect().getUser().getId(),
              acceptedBid.getArchitect().getUser().getEmail(),
              req.getDecision().name()));
    }

    Long resolvedProjectId = project.getId();
    return projectService.getAllProjects().stream()
        .filter(p -> p.getId().equals(resolvedProjectId))
        .findFirst()
        .orElseThrow();
  }

  private String displayName(com.rumantra.user.domain.User user) {
    return String.join(
            " ",
            user.getFirstName() != null ? user.getFirstName() : "",
            user.getLastName() != null ? user.getLastName() : "")
        .trim();
  }

  private void initializeProjectPhasesFromBid(Project project) {
    List<ProjectPhase> existing =
        projectPhaseRepository.findByProjectIdOrderByPhaseNumberAsc(project.getId());
    if (!existing.isEmpty()) return;

    List<Bid> acceptedBids =
        bidRepository.findByProjectIdAndStatus(project.getId(), BidStatus.ACCEPTED);
    if (acceptedBids.isEmpty()) return;

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
              .maxRevisions(bidPhase.getRevisionRounds() != null ? bidPhase.getRevisionRounds() : 3)
              .build());
    }
    projectPhaseRepository.saveAll(phases);
  }
}
