package com.rumantra.project.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.Type;

import com.rumantra.architect.domain.Architect;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_project_phase_disbursement")
public class PhaseDisbursement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "phase_id", nullable = false)
  @ToString.Exclude
  private ProjectPhase phase;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "architect_id", nullable = false)
  @ToString.Exclude
  private Architect architect;

  @Column(name = "xendit_payout_id", unique = true)
  private String xenditPayoutId;

  @Column(name = "xendit_reference_id", unique = true)
  private String xenditReferenceId;

  @Column(name = "channel_code", length = 50)
  private String channelCode;

  @Column(name = "account_number")
  private String accountNumber;

  @Column(name = "account_holder_name")
  private String accountHolderName;

  @Column(name = "amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private DisbursementStatus status = DisbursementStatus.PENDING;

  @Column(name = "failure_code", length = 100)
  private String failureCode;

  @Type(JsonType.class)
  @Column(name = "xendit_raw_payload", columnDefinition = "jsonb")
  private Map<String, Object> xenditRawPayload;

  @Column(name = "initiated_at")
  private LocalDateTime initiatedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;
}
