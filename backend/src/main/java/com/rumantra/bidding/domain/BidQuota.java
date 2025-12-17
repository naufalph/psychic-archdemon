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

  @Column(name = "tokens_allocated", nullable = false)
  @Builder.Default
  private Integer tokensAllocated = 0;

  @Column(name = "tokens_remaining", nullable = false)
  @Builder.Default
  private Integer tokensRemaining = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
