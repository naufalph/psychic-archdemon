package com.rumantra.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.project.domain.PhaseDisbursementStatusLog;

/** Append-only: insert and read only. The database rejects UPDATE, DELETE and TRUNCATE. */
@Repository
public interface PhaseDisbursementStatusLogRepository
    extends JpaRepository<PhaseDisbursementStatusLog, Long> {

  List<PhaseDisbursementStatusLog> findByDisbursementIdOrderByCreatedAtAsc(Long disbursementId);

  @Query(
      "SELECT l FROM PhaseDisbursementStatusLog l JOIN FETCH l.disbursement d"
          + " WHERE d.phase.project.id = :projectId ORDER BY l.createdAt ASC")
  List<PhaseDisbursementStatusLog> findByProjectId(@Param("projectId") Long projectId);
}
