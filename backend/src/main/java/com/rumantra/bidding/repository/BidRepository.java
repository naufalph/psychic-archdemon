package com.rumantra.bidding.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.client.domain.ProjectStatus;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

  List<Bid> findByProjectId(Long projectId);

  List<Bid> findByArchitectId(Long architectId);

  List<Bid> findByProjectIdAndStatus(Long projectId, BidStatus status);

  List<Bid> findByProjectIdAndStatusNot(Long projectId, BidStatus status);

  boolean existsByProjectIdAndArchitectId(Long projectId, Long architectId);

  boolean existsByProjectIdAndArchitectUserId(Long projectId, Long userId);

  long countByProjectId(Long projectId);

  @Query(
      "SELECT b FROM Bid b JOIN FETCH b.architect a JOIN FETCH a.user"
          + " WHERE b.project.id = :projectId AND b.status = :status")
  List<Bid> findPendingBidsWithArchitect(
      @Param("projectId") Long projectId, @Param("status") BidStatus status);

  @Query(
      "SELECT b FROM Bid b JOIN FETCH b.project p JOIN FETCH p.client c JOIN FETCH c.user"
          + " JOIN FETCH b.architect a JOIN FETCH a.user"
          + " WHERE b.status = :bidStatus AND p.status = :projectStatus"
          + " AND b.acceptedAt >= :from AND b.acceptedAt < :to")
  List<Bid> findAcceptedBidsForNegotiationReminder(
      @Param("bidStatus") BidStatus bidStatus,
      @Param("projectStatus") ProjectStatus projectStatus,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  @Query(
      "SELECT b FROM Bid b JOIN FETCH b.project p JOIN FETCH p.client c JOIN FETCH c.user"
          + " JOIN FETCH b.architect a JOIN FETCH a.user"
          + " WHERE b.status = :bidStatus AND p.status = :projectStatus"
          + " AND b.acceptedAt <= :threshold")
  List<Bid> findAcceptedBidsForNegotiationExpiry(
      @Param("bidStatus") BidStatus bidStatus,
      @Param("projectStatus") ProjectStatus projectStatus,
      @Param("threshold") LocalDateTime threshold);
}
