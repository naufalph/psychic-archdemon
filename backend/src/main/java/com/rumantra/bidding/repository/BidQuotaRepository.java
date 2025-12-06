package com.rumantra.bidding.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.bidding.domain.BidQuota;

@Repository
public interface BidQuotaRepository extends JpaRepository<BidQuota, Long> {

  Optional<BidQuota> findByArchitectId(Long architectId);

  List<BidQuota> findByNextResetDateBefore(LocalDateTime date);
}
