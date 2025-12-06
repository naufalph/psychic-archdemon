package com.rumantra.bidding.domain;

import java.time.LocalDateTime;

import com.rumantra.architect.domain.Architect;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_bid_usage_log")
public class BidUsageLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "architect_id", nullable = false)
  @ToString.Exclude
  private Architect architect;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bid_id")
  @ToString.Exclude
  private Bid bid;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", nullable = false)
  private BidUsageAction action;

  @Column(name = "quota_change", nullable = false)
  private Integer quotaChange;

  @Column(name = "quota_after", nullable = false)
  private Integer quotaAfter;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "timestamp", nullable = false, updatable = false)
  private LocalDateTime timestamp;

  @PrePersist
  protected void onCreate() {
    if (timestamp == null) {
      timestamp = LocalDateTime.now();
    }
  }
}
