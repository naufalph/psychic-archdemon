package com.rumantra.bidding.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.bidding.domain.BidAttachment;

@Repository
public interface BidAttachmentRepository extends JpaRepository<BidAttachment, Long> {
  List<BidAttachment> findByBidIdOrderByDisplayOrder(Long bidId);

  long countByBidId(Long bidId);
}
