package com.rumantra.bidding.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_bid_detail")
public class BidDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bid_id", nullable = false, unique = true)
  @ToString.Exclude
  private Bid bid;

  @Column(name = "concept_statement", columnDefinition = "TEXT")
  private String conceptStatement;

  @Column(name = "facade_description", columnDefinition = "TEXT")
  private String facadeDescription;

  @Column(name = "interior_description", columnDefinition = "TEXT")
  private String interiorDescription;

  @Column(name = "massing_description", columnDefinition = "TEXT")
  private String massingDescription;

  @Column(name = "zoning_description", columnDefinition = "TEXT")
  private String zoningDescription;

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
