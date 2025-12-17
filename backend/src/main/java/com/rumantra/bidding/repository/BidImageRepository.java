package com.rumantra.bidding.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.bidding.domain.BidImage;
import com.rumantra.bidding.domain.BidImageType;

@Repository
public interface BidImageRepository extends JpaRepository<BidImage, Long> {

  List<BidImage> findByBidIdOrderByDisplayOrder(Long bidId);

  List<BidImage> findByBidIdAndImageTypeOrderByDisplayOrder(Long bidId, BidImageType imageType);

  long countByBidIdAndImageType(Long bidId, BidImageType imageType);
}
