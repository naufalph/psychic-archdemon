package com.rumantra.admin.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.admin.dto.DisputeResolutionRequest;
import com.rumantra.project.domain.PhaseActorType;
import com.rumantra.project.domain.PhaseProcessLog;
import com.rumantra.project.domain.PhaseStatus;
import com.rumantra.project.domain.ProjectPhase;
import com.rumantra.project.repository.PhaseProcessLogRepository;
import com.rumantra.project.repository.ProjectPhaseRepository;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminPhaseService {

  private final ProjectPhaseRepository projectPhaseRepository;
  private final PhaseProcessLogRepository phaseProcessLogRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<ProjectPhase> getDisputedPhases() {
    return projectPhaseRepository.findByStatus(PhaseStatus.DISPUTED);
  }

  @Transactional
  public ProjectPhase resolveDispute(
      Long phaseId, Long superuserUserId, DisputeResolutionRequest req) {
    ProjectPhase phase =
        projectPhaseRepository
            .findById(phaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PROJECT_PHASE_NOT_FOUND));

    if (phase.getStatus() != PhaseStatus.DISPUTED) {
      throw new BusinessException(ExceptionConstants.PHASE_WRONG_STATUS);
    }

    PhaseStatus toStatus =
        req.getDecision() == DisputeResolutionRequest.Decision.APPROVE
            ? PhaseStatus.APPROVED
            : PhaseStatus.IN_PROGRESS;

    phase.setStatus(toStatus);
    projectPhaseRepository.save(phase);

    log(
        phase,
        superuserUserId,
        PhaseActorType.SUPERUSER,
        "DISPUTE_RESOLVED_" + req.getDecision().name(),
        PhaseStatus.DISPUTED,
        toStatus,
        req.getNote() != null ? Map.of("note", req.getNote()) : null);

    return phase;
  }

  private void log(
      ProjectPhase phase,
      Long actorUserId,
      PhaseActorType actorType,
      String action,
      PhaseStatus from,
      PhaseStatus to,
      Map<String, Object> meta) {
    User actor = actorUserId != null ? userRepository.findById(actorUserId).orElse(null) : null;
    phaseProcessLogRepository.save(
        PhaseProcessLog.builder()
            .phase(phase)
            .actor(actor)
            .actorType(actorType)
            .action(action)
            .fromStatus(from != null ? from.name() : null)
            .toStatus(to != null ? to.name() : null)
            .metadata(meta)
            .build());
  }
}
