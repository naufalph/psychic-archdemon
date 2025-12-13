package com.rumantra.bidding.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.bidding.domain.BidPortfolioRef;

@Repository
public interface BidPortfolioRefRepository extends JpaRepository<BidPortfolioRef, Long> {

  List<BidPortfolioRef> findByBidIdOrderByDisplayOrder(Long bidId);

  long countByBidId(Long bidId);

  void deleteByBidId(Long bidId);
}
