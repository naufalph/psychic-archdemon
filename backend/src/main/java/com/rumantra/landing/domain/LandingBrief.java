package com.rumantra.landing.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_landing_brief")
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

  @Column(name = "location", length = 160)
  private String location;

  @Column(name = "lot_size")
  private Integer lotSize;

  @Column(name = "design_budget_total")
  private Long designBudgetTotal;

  @Column(name = "design_budget_min")
  private Long designBudgetMin;

  @Column(name = "design_budget_max")
  private Long designBudgetMax;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "phone_number", length = 24)
  private String phoneNumber;

  @Column(name = "start_date_type", length = 20)
  private String startDateType;

  @Column(name = "expected_start_date")
  private LocalDate expectedStartDate;

  @Column(name = "claimed_by_user_id")
  private Long claimedByUserId;

  @Column(name = "claimed_at")
  private LocalDateTime claimedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
