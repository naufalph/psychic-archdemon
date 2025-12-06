package com.rumantra.bidding.domain;

import java.time.LocalDateTime;

import com.rumantra.architect.domain.Architect;
import com.rumantra.subscription.domain.SubscriptionTier;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_bid_quota")
public class BidQuota {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "architect_id", nullable = false, unique = true)
  @ToString.Exclude
  private Architect architect;

  @Enumerated(EnumType.STRING)
  @Column(name = "tier", nullable = false)
  @Builder.Default
  private SubscriptionTier tier = SubscriptionTier.FREE;

  @Column(name = "total_bids_allowed", nullable = false)
  @Builder.Default
  private Integer totalBidsAllowed = 3;

  @Column(name = "bids_used", nullable = false)
  @Builder.Default
  private Integer bidsUsed = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "reset_interval", nullable = false)
  @Builder.Default
  private ResetInterval resetInterval = ResetInterval.BI_WEEKLY;

  @Column(name = "last_reset_date", nullable = false)
  private LocalDateTime lastResetDate;

  @Column(name = "next_reset_date", nullable = false)
  private LocalDateTime nextResetDate;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    if (lastResetDate == null) {
      lastResetDate = LocalDateTime.now();
    }
    if (nextResetDate == null) {
      // Default to 14 days for BI_WEEKLY
      nextResetDate = LocalDateTime.now().plusDays(14);
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Computed getter for remaining bids
  public Integer getBidsRemaining() {
    return totalBidsAllowed - bidsUsed;
  }
}
