package com.rumantra.client.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.client.domain.ProjectStatusLog;

/** Append-only: insert and read only. The database rejects UPDATE, DELETE and TRUNCATE. */
@Repository
public interface ProjectStatusLogRepository extends JpaRepository<ProjectStatusLog, Long> {

  List<ProjectStatusLog> findByProjectIdOrderByCreatedAtAsc(Long projectId);
}
