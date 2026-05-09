package com.rumantra.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.project.domain.PhaseProcessLog;

@Repository
public interface PhaseProcessLogRepository extends JpaRepository<PhaseProcessLog, Long> {

  List<PhaseProcessLog> findByPhaseIdOrderByCreatedAtAsc(Long phaseId);
}
