package com.rumantra.bidding.domain;

import com.rumantra.architect.domain.Porto;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
    name = "rmtr_bid_portfolio_ref",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_bid_portfolio", columnNames = {"bid_id", "porto_id"})
    })
public class BidPortfolioRef {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bid_id", nullable = false)
  @ToString.Exclude
  private Bid bid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "porto_id", nullable = false)
  @ToString.Exclude
  private Porto porto;

  @Column(name = "display_order")
  @Builder.Default
  private Integer displayOrder = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
