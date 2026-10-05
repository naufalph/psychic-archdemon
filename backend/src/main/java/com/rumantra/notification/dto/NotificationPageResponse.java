package com.rumantra.notification.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPageResponse {

  private List<NotificationResponse> items;
  private Long nextCursor;
  private Long unreadCount;
  private Long totalCount;
}
