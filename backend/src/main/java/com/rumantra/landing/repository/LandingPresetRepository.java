package com.rumantra.landing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.landing.domain.LandingPreset;

@Repository
public interface LandingPresetRepository extends JpaRepository<LandingPreset, Long> {

  List<LandingPreset> findAllByActiveTrueOrderByDisplayOrderAsc();

  List<LandingPreset> findAllByOrderByDisplayOrderAsc();

  Optional<LandingPreset> findBySlug(String slug);

  boolean existsBySlug(String slug);
}
