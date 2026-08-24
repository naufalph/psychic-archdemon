package com.rumantra.client.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.client.domain.Project;
import com.rumantra.client.domain.ProjectStatus;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  List<Project> findByClientIdOrderByCreatedAtDesc(Long clientId);

  List<Project> findByStatus(ProjectStatus status);

  @Query("SELECT p FROM Project p LEFT JOIN FETCH p.files WHERE p.id = :projectId")
  Optional<Project> findByIdWithFiles(@Param("projectId") Long projectId);

  @Query(
      "SELECT p FROM Project p LEFT JOIN FETCH p.files WHERE p.client.id = :clientId ORDER BY p.createdAt DESC")
  List<Project> findByClientIdWithFiles(@Param("clientId") Long clientId);

  @Query("SELECT p FROM Project p LEFT JOIN FETCH p.files WHERE p.status = :status")
  List<Project> findByStatusWithFiles(@Param("status") ProjectStatus status, Sort sort);

  @Query(
      "SELECT p FROM Project p LEFT JOIN FETCH p.files"
          + " WHERE p.status IN :statuses"
          + " ORDER BY p.createdAt DESC")
  List<Project> findPublicProjects(@Param("statuses") Collection<ProjectStatus> statuses);

  @Query(
      "SELECT p FROM Project p JOIN FETCH p.client c JOIN FETCH c.user"
          + " WHERE p.status = :status AND p.biddingDeadline < :threshold")
  List<Project> findOpenProjectsForClosure(
      @Param("status") ProjectStatus status, @Param("threshold") LocalDateTime threshold);

  @Query(
      "SELECT p FROM Project p JOIN FETCH p.client c JOIN FETCH c.user"
          + " WHERE p.status = :status AND p.biddingDeadline >= :from AND p.biddingDeadline < :to")
  List<Project> findOpenProjectsForReminder(
      @Param("status") ProjectStatus status,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);
}
