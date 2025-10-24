package com.rumantra.client.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.client.domain.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

  List<Project> findByClientIdOrderByCreatedAtDesc(Long clientId);

  @Query("SELECT p FROM Project p LEFT JOIN FETCH p.files WHERE p.id = :projectId")
  Optional<Project> findByIdWithFiles(@Param("projectId") Long projectId);

  @Query(
      "SELECT p FROM Project p LEFT JOIN FETCH p.files WHERE p.client.id = :clientId ORDER BY p.createdAt DESC")
  List<Project> findByClientIdWithFiles(@Param("clientId") Long clientId);
}
