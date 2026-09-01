package com.rumantra.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.payment.domain.PhasePaymentStatusLog;

/** Append-only: insert and read only. The database rejects UPDATE, DELETE and TRUNCATE. */
@Repository
public interface PhasePaymentStatusLogRepository
    extends JpaRepository<PhasePaymentStatusLog, Long> {

  List<PhasePaymentStatusLog> findByPhasePaymentIdOrderByCreatedAtAsc(Long phasePaymentId);

  @Query(
      "SELECT l FROM PhasePaymentStatusLog l JOIN FETCH l.phasePayment p"
          + " WHERE p.project.id = :projectId ORDER BY l.createdAt ASC")
  List<PhasePaymentStatusLog> findByProjectId(@Param("projectId") Long projectId);
}
