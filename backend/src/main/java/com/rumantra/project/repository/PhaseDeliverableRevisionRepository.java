package com.rumantra.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.project.domain.PhaseDeliverableRevision;

@Repository
public interface PhaseDeliverableRevisionRepository
    extends JpaRepository<PhaseDeliverableRevision, Long> {

  List<PhaseDeliverableRevision> findByPhaseIdOrderByRevisionRoundAscDeliverableIndexAsc(
      Long phaseId);
}
