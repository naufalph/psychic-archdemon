package com.rumantra.bidding.repository;

import com.rumantra.bidding.domain.BidDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidDetailRepository extends JpaRepository<BidDetail, Long> {

  Optional<BidDetail> findByBidId(Long bidId);
}
