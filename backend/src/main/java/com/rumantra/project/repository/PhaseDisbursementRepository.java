package com.rumantra.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.project.domain.PhaseDisbursement;

@Repository
public interface PhaseDisbursementRepository extends JpaRepository<PhaseDisbursement, Long> {

  Optional<PhaseDisbursement> findByXenditPayoutId(String xenditPayoutId);

  Optional<PhaseDisbursement> findByXenditReferenceId(String referenceId);

  Optional<PhaseDisbursement> findByPhaseId(Long phaseId);
}
