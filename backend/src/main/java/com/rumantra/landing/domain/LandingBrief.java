package com.rumantra.landing.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_landing_brief")
// The survey's progress update and the draft's project link can land on the same row at once;
// writing only the changed columns stops either save from reverting the other's column.
@DynamicUpdate
public class LandingBrief {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "claim_token", nullable = false, unique = true, length = 64)
  private String claimToken;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "preset_id")
  private LandingPreset preset;

  @Column(name = "building_function", length = 40)
  private String buildingFunction;

  @Column(name = "project_scope", length = 20)
  private String projectScope;

  @Column(name = "sub_category", length = 60)
  private String subCategory;

  @Column(name = "title", length = 160)
  private String title;

  @Column(name = "claimed_by_user_id")
  private Long claimedByUserId;

  @Column(name = "claimed_at")
  private LocalDateTime claimedAt;

  @Column(name = "furthest_step", length = 20)
  private String furthestStep;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = createdAt;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
