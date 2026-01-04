package com.rumantra.chat.dto;

import java.time.LocalDateTime;

import com.rumantra.chat.domain.ConversationStatus;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

  private Long id;
  private Long projectId;
  private Long bidId;
  private Long architectId;
  private String architectName;
  private Long clientId;
  private String clientName;
  private ConversationStatus status;
  private Integer unreadCount;
  private MessageResponse lastMessage;
  private LocalDateTime lastMessageAt;
  private LocalDateTime createdAt;
}
