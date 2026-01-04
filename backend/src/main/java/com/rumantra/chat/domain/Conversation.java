package com.rumantra.chat.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rmtr_conversation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(name = "bid_id", nullable = false, unique = true)
  private Long bidId;

  @Column(name = "architect_id", nullable = false)
  private Long architectId;

  @Column(name = "client_id", nullable = false)
  private Long clientId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private ConversationStatus status = ConversationStatus.ACTIVE;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "last_message_at")
  private LocalDateTime lastMessageAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
