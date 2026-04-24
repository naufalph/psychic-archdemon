package com.rumantra.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.payment.domain.PhasePayment;

@Repository
public interface PhasePaymentRepository extends JpaRepository<PhasePayment, Long> {

  Optional<PhasePayment> findByPhaseId(Long phaseId);

  List<PhasePayment> findByProjectIdOrderByCreatedAtAsc(Long projectId);

  Optional<PhasePayment> findByXenditReferenceId(String xenditReferenceId);
}
