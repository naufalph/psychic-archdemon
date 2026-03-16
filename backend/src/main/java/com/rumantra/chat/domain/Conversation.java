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

  @Column(name = "bid_id")
  private Long bidId;

  @Column(name = "architect_id")
  private Long architectId;

  @Column(name = "client_id")
  private Long clientId;

  /** Non-null for SUPPORT conversations; null for PROJECT conversations. */
  @Column(name = "requester_user_id")
  private Long requesterUserId;

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

  @Column(name = "it_support_requested", nullable = false)
  @Builder.Default
  private Boolean itSupportRequested = false;

  @Column(name = "it_support_requested_at")
  private LocalDateTime itSupportRequestedAt;

  public boolean isSupport() {
    return requesterUserId != null;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
