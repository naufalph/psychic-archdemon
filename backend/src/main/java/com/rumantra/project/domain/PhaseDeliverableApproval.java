package com.rumantra.project.domain;

import java.time.LocalDateTime;

import com.rumantra.user.domain.User;

import jakarta.persistence.*;
import lombok.*;

/**
 * Records that a client approved one deliverable of a phase, identified by its position in the
 * accepted bid phase's deliverables array.
 *
 * <p>This is a projection of the {@code DELIVERABLE_APPROVED} events in {@code
 * rmtr_project_phase_log}, which is the append-only durable record. Rows here are deleted when a
 * revision is requested; that erases no history.
 */
@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_project_phase_deliverable_approval")
public class PhaseDeliverableApproval {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "phase_id", nullable = false)
  @ToString.Exclude
  private ProjectPhase phase;

  @Column(name = "deliverable_index", nullable = false)
  private Integer deliverableIndex;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "approved_by")
  @ToString.Exclude
  private User approvedBy;

  @Column(name = "approved_at", nullable = false, updatable = false)
  private LocalDateTime approvedAt;

  @PrePersist
  protected void onCreate() {
    if (approvedAt == null) {
      approvedAt = LocalDateTime.now();
    }
  }
}
