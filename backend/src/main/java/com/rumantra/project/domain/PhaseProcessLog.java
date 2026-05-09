package com.rumantra.project.domain;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.Type;

import com.rumantra.user.domain.User;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_project_phase_log")
public class PhaseProcessLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "phase_id", nullable = false)
  @ToString.Exclude
  private ProjectPhase phase;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id")
  @ToString.Exclude
  private User actor;

  @Enumerated(EnumType.STRING)
  @Column(name = "actor_type", nullable = false, length = 50)
  private PhaseActorType actorType;

  @Column(name = "action", nullable = false, length = 100)
  private String action;

  @Column(name = "from_status", length = 50)
  private String fromStatus;

  @Column(name = "to_status", length = 50)
  private String toStatus;

  @Type(JsonType.class)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private Map<String, Object> metadata;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
