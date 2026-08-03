package com.rumantra.admin.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.admin.dto.AdminProjectDetailResponse;
import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidPaymentPhase;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidPaymentPhaseRepository;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.Client;
import com.rumantra.client.domain.Project;
import com.rumantra.client.domain.ProjectStatus;
import com.rumantra.client.dto.ProjectResponse;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.client.service.ProjectService;
import com.rumantra.project.domain.ProjectPhase;
import com.rumantra.project.repository.ProjectPhaseRepository;
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

    project.setStatus(ProjectStatus.CANCELLED);
    projectRepository.save(project);

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
    project.setStatus(ProjectStatus.IN_PROGRESS);
    project = projectRepository.save(project);

    initializeProjectPhasesFromBid(project);

    log.info("Admin overrode negotiation for project {}, moved to IN_PROGRESS", projectId);
    return projectService.getAllProjects().stream()
        .filter(p -> p.getId().equals(projectId))
        .findFirst()
        .orElseThrow();
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
