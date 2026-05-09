package com.rumantra.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.project.domain.PhaseDeliverable;

@Repository
public interface PhaseDeliverableRepository extends JpaRepository<PhaseDeliverable, Long> {

  List<PhaseDeliverable> findByPhaseIdOrderByUploadedAtAsc(Long phaseId);
}
