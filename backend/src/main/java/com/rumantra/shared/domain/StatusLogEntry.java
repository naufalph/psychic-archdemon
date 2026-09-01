package com.rumantra.shared.domain;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.Type;

import com.rumantra.user.domain.User;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Shared columns for the append-only status logs. Subclasses add the typed reference to the entity
 * whose status changed.
 *
 * <p>Rows are inserted and never modified — the database rejects UPDATE, DELETE and TRUNCATE on
 * these tables. Write them only through {@code StatusTransitionService}, so a status column can
 * never move without leaving a row behind.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class StatusLogEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id")
  private User actor;

  @Enumerated(EnumType.STRING)
  @Column(name = "actor_type", nullable = false, length = 50)
  private ActorType actorType;

  @Column(name = "action", nullable = false, length = 100)
  private String action;

  @Column(name = "from_status", length = 50)
  private String fromStatus;

  @Column(name = "to_status", nullable = false, length = 50)
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
