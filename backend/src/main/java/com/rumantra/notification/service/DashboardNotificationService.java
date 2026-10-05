package com.rumantra.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.Project;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.notification.domain.DashboardNotification;
import com.rumantra.notification.domain.NotificationType;
import com.rumantra.notification.dto.NotificationPageResponse;
import com.rumantra.notification.dto.NotificationResponse;
import com.rumantra.notification.repository.DashboardNotificationRepository;
import com.rumantra.project.repository.ProjectPhaseRepository;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardNotificationService {

  private final DashboardNotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final ProjectRepository projectRepository;
  private final BidRepository bidRepository;
  private final ProjectPhaseRepository projectPhaseRepository;

  static final int DEFAULT_PAGE_SIZE = 20;
  static final int MAX_PAGE_SIZE = 50;

  @PersistenceContext private EntityManager entityManager;

  /**
   * Create a new dashboard notification for a user.
   *
   * @param userId The recipient user ID
   * @param type The notification type
   * @param title The notification title
   * @param message The notification message
   * @param messageCode The i18n message code
   * @param messageData The JSON string with dynamic data for i18n
   * @param referenceType The type of referenced entity (e.g., "PROJECT", "BID")
   * @param referenceId The ID of referenced entity
   * @return The created notification
   */
  public DashboardNotification createNotification(
      Long userId,
      NotificationType type,
      String title,
      String message,
      String messageCode,
      String messageData,
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
            .messageCode(messageCode)
            .messageData(messageData)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .projectId(resolveProjectId(referenceType, referenceId))
            .isRead(false)
            .createdAt(LocalDateTime.now())
            .build();

    DashboardNotification saved = notificationRepository.save(notification);
    entityManager.flush();
    log.info("Created dashboard notification {} for user {}", saved.getId(), userId);
    return saved;
  }

  Long resolveProjectId(String referenceType, Long referenceId) {
    if (referenceType == null || referenceId == null) {
      return null;
    }
    return switch (referenceType) {
      case "PROJECT" -> referenceId;
      case "BID" ->
          bidRepository.findById(referenceId).map(bid -> bid.getProject().getId()).orElse(null);
      case "PHASE" ->
          projectPhaseRepository
              .findById(referenceId)
              .map(phase -> phase.getProject().getId())
              .orElse(null);
      default -> null;
    };
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
    Map<Long, Project> projects = loadProjects(notificationPage.getContent());
    return notificationPage.map(notification -> toResponse(notification, projects));
  }

  @Transactional(readOnly = true)
  public NotificationPageResponse getNotificationPage(
      Long cursor, Integer limit, boolean unreadOnly) {
    Long userId = SecurityUtils.getCurrentUserId();
    int size = limit == null ? DEFAULT_PAGE_SIZE : Math.max(1, Math.min(limit, MAX_PAGE_SIZE));
    // One extra row tells us whether another page exists without a second count query
    Pageable pageable = PageRequest.of(0, size + 1);

    List<DashboardNotification> rows;
    if (cursor == null) {
      rows = notificationRepository.findFirstPage(userId, unreadOnly, pageable);
    } else {
      rows =
          notificationRepository
              .findById(cursor)
              .filter(anchor -> anchor.getUser().getId().equals(userId))
              .map(
                  anchor ->
                      notificationRepository.findPageAfter(
                          userId, unreadOnly, anchor.getCreatedAt(), anchor.getId(), pageable))
              .orElse(List.of());
    }

    boolean hasMore = rows.size() > size;
    List<DashboardNotification> pageRows = hasMore ? rows.subList(0, size) : rows;

    return NotificationPageResponse.builder()
        .items(toResponseList(pageRows))
        .nextCursor(hasMore ? pageRows.get(pageRows.size() - 1).getId() : null)
        .unreadCount(notificationRepository.countUnreadByUserId(userId))
        .totalCount(notificationRepository.countByUserId(userId))
        .build();
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
            .filter(found -> found.getUser().getId().equals(userId))
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));

    if (!notification.getIsRead()) {
      notification.setIsRead(true);
      notification.setReadAt(LocalDateTime.now());
      notification = notificationRepository.save(notification);
      log.info("Notification {} marked as read by user {}", notificationId, userId);
    }

    return toResponseList(List.of(notification)).get(0);
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
  private NotificationResponse toResponse(
      DashboardNotification notification, Map<Long, Project> projects) {
    if (notification == null) {
      return null;
    }

    Project project =
        notification.getProjectId() == null ? null : projects.get(notification.getProjectId());

    return NotificationResponse.builder()
        .id(notification.getId())
        .type(notification.getType())
        .title(notification.getTitle())
        .message(notification.getMessage())
        .messageCode(notification.getMessageCode())
        .messageData(notification.getMessageData())
        .referenceType(notification.getReferenceType())
        .referenceId(notification.getReferenceId())
        .projectId(notification.getProjectId())
        .projectStatus(project == null ? null : project.getStatus().name())
        .projectName(project == null ? null : project.getTitle())
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

    Map<Long, Project> projects = loadProjects(notifications);
    return notifications.stream()
        .map(notification -> toResponse(notification, projects))
        .collect(Collectors.toList());
  }

  private Map<Long, Project> loadProjects(List<DashboardNotification> notifications) {
    List<Long> projectIds =
        notifications.stream()
            .map(DashboardNotification::getProjectId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    if (projectIds.isEmpty()) {
      return Map.of();
    }
    return projectRepository.findAllById(projectIds).stream()
        .collect(Collectors.toMap(Project::getId, Function.identity()));
  }
}
