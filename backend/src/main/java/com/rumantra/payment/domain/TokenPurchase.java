package com.rumantra.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.rumantra.architect.domain.Architect;
import com.rumantra.subscription.domain.SubscriptionTier;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_token_purchase")
public class TokenPurchase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "architect_id", nullable = false)
  @ToString.Exclude
  private Architect architect;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "price_per_token", nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerToken;

  @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "tier", nullable = false)
  private SubscriptionTier tier;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private PurchaseStatus status = PurchaseStatus.PENDING;

  @Column(name = "xendit_payment_request_id")
  private String xenditPaymentRequestId;

  @Column(name = "xendit_reference_id", nullable = false, unique = true)
  private String xenditReferenceId;

  @Column(name = "payment_link", columnDefinition = "TEXT")
  private String paymentLink;

  @Column(name = "payment_method", length = 50)
  private String paymentMethod;

  @Column(name = "payment_channel", length = 50)
  private String paymentChannel;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "failure_reason", columnDefinition = "TEXT")
  private String failureReason;

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
