package com.rumantra.architect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.architect.domain.PortoDetail;

@Repository
public interface PortoDetailRepository extends JpaRepository<PortoDetail, Long> {

  /** Find all details for a portfolio, ordered by display order. */
  List<PortoDetail> findByPortoIdOrderByDisplayOrderAsc(Long portoId);

  /** Find the first image (lowest display order) for a portfolio. */
  @Query(
      "SELECT pd FROM PortoDetail pd WHERE pd.porto.id = :portoId ORDER BY pd.displayOrder ASC LIMIT 1")
  Optional<PortoDetail> findFirstByPortoId(@Param("portoId") Long portoId);

  /** Delete all details for a portfolio. */
  void deleteByPortoId(Long portoId);
}
