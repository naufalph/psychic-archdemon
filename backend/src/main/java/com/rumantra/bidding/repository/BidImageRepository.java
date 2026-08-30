package com.rumantra.bidding.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.bidding.domain.BidImage;
import com.rumantra.bidding.domain.BidImageType;
import com.rumantra.bidding.domain.BidStatus;

@Repository
public interface BidImageRepository extends JpaRepository<BidImage, Long> {

  List<BidImage> findByBidIdOrderByDisplayOrder(Long bidId);

  List<BidImage> findByBidIdAndImageTypeOrderByDisplayOrder(Long bidId, BidImageType imageType);

  long countByBidIdAndImageType(Long bidId, BidImageType imageType);

  /**
   * Images whose owning bid is long dead and whose blob can be reclaimed. Submitted bids are aged
   * by submittedAt; abandoned drafts never have one, so they are aged by updatedAt.
   */
  @Query(
      "SELECT i FROM BidImage i JOIN i.bid b WHERE i.archivedAt IS NULL"
          + " AND ((b.status IN :deadStatuses AND b.submittedAt < :cutoff)"
          + " OR (b.status = :draftStatus AND b.updatedAt < :cutoff))")
  List<BidImage> findArchivableImages(
      @Param("deadStatuses") List<BidStatus> deadStatuses,
      @Param("draftStatus") BidStatus draftStatus,
      @Param("cutoff") LocalDateTime cutoff,
      Pageable pageable);
}
