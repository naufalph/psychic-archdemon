package com.rumantra.bidding.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.bidding.domain.BidUsageAction;
import com.rumantra.bidding.domain.BidUsageLog;

@Repository
public interface BidUsageLogRepository extends JpaRepository<BidUsageLog, Long> {

  List<BidUsageLog> findByArchitectIdAndTimestampAfter(Long architectId, LocalDateTime timestamp);

  List<BidUsageLog> findByArchitectIdOrderByTimestampDesc(Long architectId);

  long countByArchitectIdAndAction(Long architectId, BidUsageAction action);

  long countByArchitectIdAndActionAndTimestampBetween(
      Long architectId, BidUsageAction action, LocalDateTime from, LocalDateTime to);
}
