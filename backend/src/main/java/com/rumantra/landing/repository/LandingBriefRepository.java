package com.rumantra.landing.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.landing.domain.LandingBrief;

@Repository
public interface LandingBriefRepository extends JpaRepository<LandingBrief, Long> {

  Optional<LandingBrief> findByClaimToken(String claimToken);

  Optional<LandingBrief> findFirstByClaimedByUserIdAndClaimedAtIsNullOrderByCreatedAtDesc(
      Long claimedByUserId);

  long countByCreatedAtAfter(LocalDateTime since);
}
