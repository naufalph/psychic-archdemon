package com.rumantra.notification.dto;

import java.time.LocalDateTime;

import com.rumantra.notification.domain.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

  private Long id;
  private Long userId;
  private NotificationType type;
  private String title;
  private String message;
  private String messageCode;
  private String messageData;
  private String referenceType;
  private Long referenceId;
  private Boolean isRead;
  private LocalDateTime readAt;
  private LocalDateTime createdAt;
}
