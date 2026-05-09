package com.rumantra.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.project.domain.PhaseStatus;
import com.rumantra.project.domain.ProjectPhase;

@Repository
public interface ProjectPhaseRepository extends JpaRepository<ProjectPhase, Long> {

  List<ProjectPhase> findByProjectIdOrderByPhaseNumberAsc(Long projectId);

  Optional<ProjectPhase> findByProjectIdAndPhaseNumber(Long projectId, int phaseNumber);

  Optional<ProjectPhase> findFirstByProjectIdAndStatusOrderByPhaseNumberAsc(
      Long projectId, PhaseStatus status);
}
