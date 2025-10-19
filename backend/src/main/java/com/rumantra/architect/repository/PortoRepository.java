package com.rumantra.architect.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.architect.domain.Porto;

@Repository
public interface PortoRepository extends JpaRepository<Porto, Long> {

  /**
   * Find all portfolios by architect ID, ordered by creation date descending. Fetch details eagerly
   * to include first image.
   */
  @Query(
      "SELECT p FROM Porto p LEFT JOIN FETCH p.details WHERE p.architect.id = :architectId ORDER BY p.createdAt DESC")
  List<Porto> findByArchitectIdWithDetails(@Param("architectId") Long architectId);

  /** Find portfolio by ID with details fetched. */
  @Query("SELECT p FROM Porto p LEFT JOIN FETCH p.details WHERE p.id = :portoId")
  Porto findByIdWithDetails(@Param("portoId") Long portoId);

  /** Find all portfolios by architect ID (without details). */
  List<Porto> findByArchitectIdOrderByCreatedAtDesc(Long architectId);
}
