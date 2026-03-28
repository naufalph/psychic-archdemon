package com.rumantra.notification.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.notification.dto.NotificationResponse;
import com.rumantra.notification.dto.UnreadCountResponse;
import com.rumantra.notification.service.DashboardNotificationService;
import com.rumantra.shared.dto.ApiResponse;
import com.rumantra.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rmtr/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final DashboardNotificationService notificationService;

  /**
   * Get all notifications for the authenticated user.
   *
   * @return List of notifications
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications() {
    try {
      List<NotificationResponse> notifications = notificationService.getUserNotifications();

      return ResponseEntity.ok(
          ApiResponse.<List<NotificationResponse>>builder()
              .success(true)
              .message("Notifications retrieved successfully")
              .data(notifications)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error retrieving notifications", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<List<NotificationResponse>>builder()
                  .success(false)
                  .message("An error occurred while retrieving notifications")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Get unread notifications for the authenticated user.
   *
   * @return List of unread notifications
   */
  @GetMapping("/unread")
  public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications() {
    try {
      List<NotificationResponse> notifications = notificationService.getUnreadNotifications();

      return ResponseEntity.ok(
          ApiResponse.<List<NotificationResponse>>builder()
              .success(true)
              .message("Unread notifications retrieved successfully")
              .data(notifications)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error retrieving unread notifications", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<List<NotificationResponse>>builder()
                  .success(false)
                  .message("An error occurred while retrieving unread notifications")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Get unread notification count for the authenticated user.
   *
   * @return Count of unread notifications
   */
  @GetMapping("/unread-count")
  public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
    try {
      Long count = notificationService.getUnreadCount();
      UnreadCountResponse response = UnreadCountResponse.builder().unreadCount(count).build();

      return ResponseEntity.ok(
          ApiResponse.<UnreadCountResponse>builder()
              .success(true)
              .message("Unread count retrieved successfully")
              .data(response)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error retrieving unread count", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<UnreadCountResponse>builder()
                  .success(false)
                  .message("An error occurred while retrieving unread count")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Mark a notification as read.
   *
   * @param notificationId The notification ID
   * @return The updated notification
   */
  @PutMapping("/{notificationId}/read")
  public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
      @PathVariable Long notificationId) {
    try {
      NotificationResponse notification = notificationService.markAsRead(notificationId);

      return ResponseEntity.ok(
          ApiResponse.<NotificationResponse>builder()
              .success(true)
              .message("Notification marked as read")
              .data(notification)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (ResourceNotFoundException e) {
      log.error("Notification not found: {}", notificationId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<NotificationResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (RuntimeException e) {
      log.error("Access denied for notification: {}", notificationId, e);
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(
              ApiResponse.<NotificationResponse>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());

    } catch (Exception e) {
      log.error("Error marking notification as read: {}", notificationId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<NotificationResponse>builder()
                  .success(false)
                  .message("An error occurred while marking notification as read")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  /**
   * Mark all notifications as read for the authenticated user.
   *
   * @return Count of notifications marked as read
   */
  @PutMapping("/read-all")
  public ResponseEntity<ApiResponse<Integer>> markAllAsRead() {
    try {
      int count = notificationService.markAllAsRead();

      return ResponseEntity.ok(
          ApiResponse.<Integer>builder()
              .success(true)
              .message(count + " notifications marked as read")
              .data(count)
              .timestamp(LocalDateTime.now().toString())
              .build());

    } catch (Exception e) {
      log.error("Error marking all notifications as read", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<Integer>builder()
                  .success(false)
                  .message("An error occurred while marking all notifications as read")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }
}
