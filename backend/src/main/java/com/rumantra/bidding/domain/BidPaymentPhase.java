package com.rumantra.bidding.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_bid_payment_phase")
public class BidPaymentPhase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bid_id", nullable = false)
  @ToString.Exclude
  private Bid bid;

  @Column(name = "phase_number", nullable = false)
  private Integer phaseNumber;

  @Column(name = "title")
  private String title;

  @Type(JsonType.class)
  @Column(name = "deliverables", columnDefinition = "jsonb")
  private List<String> deliverables;

  @Column(name = "amount", nullable = false, precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal amount = BigDecimal.ZERO;

  @Column(name = "revision_rounds")
  private Integer revisionRounds;

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
