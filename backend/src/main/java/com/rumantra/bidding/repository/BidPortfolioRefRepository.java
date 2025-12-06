package com.rumantra.bidding.repository;

import com.rumantra.bidding.domain.BidPortfolioRef;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidPortfolioRefRepository extends JpaRepository<BidPortfolioRef, Long> {

  List<BidPortfolioRef> findByBidIdOrderByDisplayOrder(Long bidId);

  long countByBidId(Long bidId);

  void deleteByBidId(Long bidId);
}
