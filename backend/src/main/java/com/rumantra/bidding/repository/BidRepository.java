package com.rumantra.bidding.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidStatus;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

  List<Bid> findByProjectId(Long projectId);

  List<Bid> findByArchitectId(Long architectId);

  List<Bid> findByProjectIdAndStatus(Long projectId, BidStatus status);

  boolean existsByProjectIdAndArchitectId(Long projectId, Long architectId);

  long countByProjectId(Long projectId);
}
