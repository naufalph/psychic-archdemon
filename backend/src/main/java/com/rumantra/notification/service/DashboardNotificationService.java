package com.rumantra.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.notification.domain.DashboardNotification;
import com.rumantra.notification.domain.NotificationType;
import com.rumantra.notification.dto.NotificationResponse;
import com.rumantra.notification.repository.DashboardNotificationRepository;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardNotificationService {

  private final DashboardNotificationRepository notificationRepository;
  private final UserRepository userRepository;

  /**
   * Create a new dashboard notification for a user.
   *
   * @param userId The recipient user ID
   * @param type The notification type
   * @param title The notification title
   * @param message The notification message
   * @param referenceType The type of referenced entity (e.g., "PROJECT", "BID")
   * @param referenceId The ID of referenced entity
   * @return The created notification
   */
  @Transactional
  public DashboardNotification createNotification(
      Long userId,
      NotificationType type,
      String title,
      String message,
      String referenceType,
      Long referenceId) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    DashboardNotification notification =
        DashboardNotification.builder()
            .user(user)
            .type(type)
            .title(title)
            .message(message)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();

    DashboardNotification saved = notificationRepository.save(notification);
    log.info("Created dashboard notification {} for user {}", saved.getId(), userId);
    return saved;
  }

  /**
   * Get all notifications for the current user.
   *
   * @return List of notifications ordered by created date descending
   */
  @Transactional(readOnly = true)
  public List<NotificationResponse> getUserNotifications() {
    Long userId = SecurityUtils.getCurrentUserId();
    List<DashboardNotification> notifications =
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    return toResponseList(notifications);
  }

  /**
   * Get paginated notifications for the current user.
   *
   * @param page Page number (0-based)
   * @param size Page size
   * @return Page of notifications
   */
  @Transactional(readOnly = true)
  public Page<NotificationResponse> getUserNotifications(int page, int size) {
    Long userId = SecurityUtils.getCurrentUserId();
    Pageable pageable = PageRequest.of(page, size);
    Page<DashboardNotification> notificationPage =
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    return notificationPage.map(this::toResponse);
  }

  /**
   * Get unread notifications for the current user.
   *
   * @return List of unread notifications
   */
  @Transactional(readOnly = true)
  public List<NotificationResponse> getUnreadNotifications() {
    Long userId = SecurityUtils.getCurrentUserId();
    List<DashboardNotification> notifications =
        notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    return toResponseList(notifications);
  }

  /**
   * Get unread notification count for the current user.
   *
   * @return Count of unread notifications
   */
  @Transactional(readOnly = true)
  public Long getUnreadCount() {
    Long userId = SecurityUtils.getCurrentUserId();
    return notificationRepository.countUnreadByUserId(userId);
  }

  /**
   * Mark a notification as read.
   *
   * @param notificationId The notification ID
   * @return The updated notification
   */
  @Transactional
  public NotificationResponse markAsRead(Long notificationId) {
    Long userId = SecurityUtils.getCurrentUserId();

    DashboardNotification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));

    // Verify ownership
    if (!notification.getUser().getId().equals(userId)) {
      throw new RuntimeException("You do not have permission to access this notification");
    }

    if (!notification.getIsRead()) {
      notification.setIsRead(true);
      notification.setReadAt(LocalDateTime.now());
      notification = notificationRepository.save(notification);
      log.info("Notification {} marked as read by user {}", notificationId, userId);
    }

    return toResponse(notification);
  }

  /**
   * Mark all notifications as read for the current user.
   *
   * @return Count of notifications marked as read
   */
  @Transactional
  public int markAllAsRead() {
    Long userId = SecurityUtils.getCurrentUserId();
    List<DashboardNotification> unreadNotifications =
        notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

    LocalDateTime now = LocalDateTime.now();
    unreadNotifications.forEach(
        notification -> {
          notification.setIsRead(true);
          notification.setReadAt(now);
        });

    notificationRepository.saveAll(unreadNotifications);
    log.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);

    return unreadNotifications.size();
  }

  /**
   * Map DashboardNotification entity to NotificationResponse DTO.
   *
   * @param notification The notification entity
   * @return NotificationResponse DTO
   */
  private NotificationResponse toResponse(DashboardNotification notification) {
    if (notification == null) {
      return null;
    }

    return NotificationResponse.builder()
        .id(notification.getId())
        .userId(notification.getUser().getId())
        .type(notification.getType())
        .title(notification.getTitle())
        .message(notification.getMessage())
        .referenceType(notification.getReferenceType())
        .referenceId(notification.getReferenceId())
        .isRead(notification.getIsRead())
        .readAt(notification.getReadAt())
        .createdAt(notification.getCreatedAt())
        .build();
  }

  /**
   * Map list of DashboardNotification entities to list of NotificationResponse DTOs.
   *
   * @param notifications List of notification entities
   * @return List of NotificationResponse DTOs
   */
  private List<NotificationResponse> toResponseList(List<DashboardNotification> notifications) {
    if (notifications == null) {
      return List.of();
    }

    return notifications.stream().map(this::toResponse).collect(Collectors.toList());
  }
}
