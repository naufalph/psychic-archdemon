package com.rumantra.subscription.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "rmtr_subscription")
public class Subscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "architect_id", nullable = false)
  @ToString.Exclude
  private Architect architect;

  @Enumerated(EnumType.STRING)
  @Column(name = "tier", nullable = false)
  @Builder.Default
  private SubscriptionTier tier = SubscriptionTier.FREE;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_status")
  @Builder.Default
  private PaymentStatus paymentStatus = PaymentStatus.ACTIVE;

  @Column(name = "monthly_price", precision = 10, scale = 2)
  private BigDecimal monthlyPrice;

  @Column(name = "payment_method_id")
  private String paymentMethodId;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    if (startDate == null) {
      startDate = LocalDate.now();
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
