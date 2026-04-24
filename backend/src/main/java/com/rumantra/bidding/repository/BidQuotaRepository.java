package com.rumantra.bidding.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.bidding.domain.BidQuota;

import jakarta.persistence.LockModeType;

@Repository
public interface BidQuotaRepository extends JpaRepository<BidQuota, Long> {

  Optional<BidQuota> findByArchitectId(Long architectId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT q FROM BidQuota q WHERE q.architect.id = :architectId")
  Optional<BidQuota> findByArchitectIdForUpdate(@Param("architectId") Long architectId);
}
