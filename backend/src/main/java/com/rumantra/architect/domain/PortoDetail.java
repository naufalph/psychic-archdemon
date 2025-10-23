package com.rumantra.architect.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_porto_dtl")
public class PortoDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "porto_id", nullable = false)
  @ToString.Exclude
  private Porto porto;

  @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
  private String originalUrl;

  @Column(name = "large_url", nullable = false, columnDefinition = "TEXT")
  private String largeUrl;

  @Column(name = "medium_url", nullable = false, columnDefinition = "TEXT")
  private String mediumUrl;

  @Column(name = "display_order")
  @Builder.Default
  private int displayOrder = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
