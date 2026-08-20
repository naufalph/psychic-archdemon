package com.rumantra.landing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.landing.domain.LandingHeroSlide;

@Repository
public interface LandingHeroSlideRepository extends JpaRepository<LandingHeroSlide, Long> {

  List<LandingHeroSlide> findAllByActiveTrueOrderByDisplayOrderAsc();

  List<LandingHeroSlide> findAllByOrderByDisplayOrderAsc();
}
