package com.rumantra.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.rumantra.architect.domain.Architect;
import com.rumantra.bidding.domain.BidPaymentPhase;
import com.rumantra.client.domain.Client;
import com.rumantra.client.domain.Project;
import com.rumantra.project.domain.ProjectPhase;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_phase_payment")
public class PhasePayment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "phase_id", nullable = false)
  @ToString.Exclude
  private BidPaymentPhase phase;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  @ToString.Exclude
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  @ToString.Exclude
  private Client client;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "architect_id", nullable = false)
  @ToString.Exclude
  private Architect architect;

  @Column(name = "amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private PhasePaymentStatus status = PhasePaymentStatus.PENDING;

  @Column(name = "xendit_invoice_id")
  private String xenditInvoiceId;

  @Column(name = "xendit_reference_id", unique = true)
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_phase_id")
  @ToString.Exclude
  private ProjectPhase projectPhase;

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
