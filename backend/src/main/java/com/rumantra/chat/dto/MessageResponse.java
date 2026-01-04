package com.rumantra.chat.dto;

import java.time.LocalDateTime;

import com.rumantra.chat.domain.MessageType;
import com.rumantra.chat.domain.SenderType;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

  private Long id;
  private Long conversationId;
  private Long senderUserId;
  private String senderName;
  private SenderType senderType;
  private String content;
  private MessageType messageType;
  private Boolean isRead;
  private LocalDateTime readAt;
  private MessageFileResponse file;
  private LocalDateTime createdAt;
}
