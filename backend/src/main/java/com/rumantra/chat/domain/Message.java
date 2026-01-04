package com.rumantra.chat.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rmtr_message")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "conversation_id", nullable = false)
  private Long conversationId;

  @Column(name = "sender_user_id", nullable = false)
  private Long senderUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "sender_type", nullable = false)
  private SenderType senderType;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "message_type", nullable = false)
  @Builder.Default
  private MessageType messageType = MessageType.TEXT;

  @Column(name = "is_read")
  @Builder.Default
  private Boolean isRead = false;

  @Column(name = "read_at")
  private LocalDateTime readAt;

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
