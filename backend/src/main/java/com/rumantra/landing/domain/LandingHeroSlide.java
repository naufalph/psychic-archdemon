package com.rumantra.landing.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_landing_hero_slide")
public class LandingHeroSlide {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "image_original_url", columnDefinition = "TEXT")
  private String imageOriginalUrl;

  @Column(name = "image_large_url", columnDefinition = "TEXT")
  private String imageLargeUrl;

  @Column(name = "image_medium_url", columnDefinition = "TEXT")
  private String imageMediumUrl;

  @Column(name = "architect_name", nullable = false, length = 120)
  private String architectName;

  @Column(name = "avatar_initial", length = 2)
  private String avatarInitial;

  @Column(name = "verified", nullable = false)
  @Builder.Default
  private boolean verified = true;

  @Column(name = "rating", precision = 2, scale = 1)
  private BigDecimal rating;

  @Column(name = "review_quote", columnDefinition = "TEXT")
  private String reviewQuote;

  @Column(name = "reviewer_name", length = 120)
  private String reviewerName;

  @Column(name = "display_order", nullable = false)
  @Builder.Default
  private int displayOrder = 0;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private boolean active = true;

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
