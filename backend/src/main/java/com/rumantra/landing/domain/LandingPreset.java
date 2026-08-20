package com.rumantra.landing.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_landing_preset")
public class LandingPreset {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "slug", nullable = false, unique = true, length = 60)
  private String slug;

  @Column(name = "label_en", nullable = false, length = 120)
  private String labelEn;

  @Column(name = "label_id", nullable = false, length = 120)
  private String labelId;

  @Column(name = "eyebrow_en", length = 60)
  private String eyebrowEn;

  @Column(name = "eyebrow_id", length = 60)
  private String eyebrowId;

  @Column(name = "icon_name", nullable = false, length = 40)
  @Builder.Default
  private String iconName = "Home";

  @Column(name = "building_function", nullable = false, length = 40)
  private String buildingFunction;

  @Column(name = "project_scope", nullable = false, length = 20)
  @Builder.Default
  private String projectScope = "NEW_BUILD";

  @Column(name = "sub_category", length = 60)
  private String subCategory;

  @Column(name = "default_title_en", length = 160)
  private String defaultTitleEn;

  @Column(name = "default_title_id", length = 160)
  private String defaultTitleId;

  @Column(name = "default_lot_size")
  private Integer defaultLotSize;

  @Column(name = "default_design_budget")
  private Long defaultDesignBudget;

  @Column(name = "default_description_en", columnDefinition = "TEXT")
  private String defaultDescriptionEn;

  @Column(name = "default_description_id", columnDefinition = "TEXT")
  private String defaultDescriptionId;

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
