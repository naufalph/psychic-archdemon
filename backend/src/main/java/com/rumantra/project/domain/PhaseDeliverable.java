package com.rumantra.project.domain;

import java.time.LocalDateTime;

import com.rumantra.user.domain.User;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_project_phase_deliverable")
public class PhaseDeliverable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "phase_id", nullable = false)
  @ToString.Exclude
  private ProjectPhase phase;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by", nullable = false)
  @ToString.Exclude
  private User uploadedBy;

  @Column(name = "file_path", nullable = false, length = 500)
  private String filePath;

  @Column(name = "file_type", length = 100)
  private String fileType;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  /** Position in the accepted bid phase's deliverables array; null for untagged legacy files. */
  @Column(name = "deliverable_index")
  private Integer deliverableIndex;

  @Column(name = "revision_round", nullable = false)
  @Builder.Default
  private Integer revisionRound = 0;

  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private LocalDateTime uploadedAt;

  @PrePersist
  protected void onCreate() {
    uploadedAt = LocalDateTime.now();
  }
}
