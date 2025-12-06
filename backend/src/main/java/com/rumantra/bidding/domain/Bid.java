package com.rumantra.bidding.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.rumantra.architect.domain.Architect;
import com.rumantra.client.domain.Project;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "rmtr_bid",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_bid_project_architect",
          columnNames = {"project_id", "architect_id"})
    })
public class Bid {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  @ToString.Exclude
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "architect_id", nullable = false)
  @ToString.Exclude
  private Architect architect;

  @Column(name = "bid_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal bidAmount;

  @Column(name = "proposed_timeline_days")
  private Integer proposedTimelineDays;

  @Column(name = "proposal", columnDefinition = "TEXT")
  private String proposal;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private BidStatus status = BidStatus.DRAFT;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "submitted_at")
  private LocalDateTime submittedAt;

  @Column(name = "accepted_at")
  private LocalDateTime acceptedAt;

  @Column(name = "rejected_at")
  private LocalDateTime rejectedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
