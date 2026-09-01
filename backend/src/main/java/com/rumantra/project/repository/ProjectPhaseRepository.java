package com.rumantra.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import com.rumantra.project.domain.PhaseStatus;
import com.rumantra.project.domain.ProjectPhase;

import jakarta.persistence.LockModeType;

@Repository
public interface ProjectPhaseRepository extends JpaRepository<ProjectPhase, Long> {

  List<ProjectPhase> findByProjectIdOrderByPhaseNumberAsc(Long projectId);

  Optional<ProjectPhase> findByProjectIdAndPhaseNumber(Long projectId, int phaseNumber);

  Optional<ProjectPhase> findFirstByProjectIdAndStatusOrderByPhaseNumberAsc(
      Long projectId, PhaseStatus status);

  List<ProjectPhase> findByStatus(PhaseStatus status);

  /**
   * Locks the phase row for the duration of the transaction. Used by the payout path: initiating a
   * disbursement leaves the phase APPROVED until the webhook lands, so the status guard alone
   * cannot stop a second concurrent request.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<ProjectPhase> findWithLockById(Long id);
}
