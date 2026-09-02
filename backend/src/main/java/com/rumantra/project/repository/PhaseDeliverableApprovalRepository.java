package com.rumantra.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.project.domain.PhaseDeliverableApproval;

@Repository
public interface PhaseDeliverableApprovalRepository
    extends JpaRepository<PhaseDeliverableApproval, Long> {

  List<PhaseDeliverableApproval> findByPhaseId(Long phaseId);

  boolean existsByPhaseIdAndDeliverableIndex(Long phaseId, Integer deliverableIndex);

  long countByPhaseId(Long phaseId);

  void deleteByPhaseId(Long phaseId);

  /**
   * Only the deliverables actually sent back lose their approval — one that the client already
   * accepted and did not ask to change stays accepted through the round.
   */
  void deleteByPhaseIdAndDeliverableIndexIn(Long phaseId, List<Integer> deliverableIndexes);
}
