package com.rumantra.bidding.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.bidding.domain.BidPaymentPhase;

@Repository
public interface BidPaymentPhaseRepository extends JpaRepository<BidPaymentPhase, Long> {

  List<BidPaymentPhase> findByBidIdOrderByPhaseNumber(Long bidId);

  @Modifying
  @Query("DELETE FROM BidPaymentPhase p WHERE p.bid.id = :bidId")
  void deleteByBidId(@Param("bidId") Long bidId);
}
