package com.rumantra.project.domain;

import java.time.LocalDateTime;

import com.rumantra.user.domain.User;

import jakarta.persistence.*;
import lombok.*;

/**
 * One instruction from the client about one deliverable of a phase.
 *
 * <p>Rows written by a single revision request share a {@code revisionRound}: the count is pooled
 * at the phase, so selecting five deliverables still costs one round. Rows are never deleted —
 * earlier rounds remain readable as the history of what was asked for.
 */
@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_project_phase_deliverable_revision")
public class PhaseDeliverableRevision {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "phase_id", nullable = false)
  @ToString.Exclude
  private ProjectPhase phase;

  @Column(name = "deliverable_index", nullable = false)
  private Integer deliverableIndex;

  @Column(name = "revision_round", nullable = false)
  private Integer revisionRound;

  @Column(name = "notes", nullable = false, columnDefinition = "TEXT")
  private String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "requested_by")
  @ToString.Exclude
  private User requestedBy;

  @Column(name = "requested_at", nullable = false, updatable = false)
  private LocalDateTime requestedAt;

  @PrePersist
  protected void onCreate() {
    if (requestedAt == null) {
      requestedAt = LocalDateTime.now();
    }
  }
}
