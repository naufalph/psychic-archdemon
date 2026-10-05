package com.rumantra.notification.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.notification.domain.DashboardNotification;

@Repository
public interface DashboardNotificationRepository
    extends JpaRepository<DashboardNotification, Long> {

  // Find all notifications for a user, ordered by created date descending
  List<DashboardNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

  // Find all notifications for a user with pagination
  Page<DashboardNotification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  // Find unread notifications for a user
  List<DashboardNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

  @Query(
      "SELECT n FROM DashboardNotification n WHERE n.user.id = :userId"
          + " AND (:unreadOnly = false OR n.isRead = false)"
          + " ORDER BY n.createdAt DESC, n.id DESC")
  List<DashboardNotification> findFirstPage(
      @Param("userId") Long userId, @Param("unreadOnly") boolean unreadOnly, Pageable pageable);

  @Query(
      "SELECT n FROM DashboardNotification n WHERE n.user.id = :userId"
          + " AND (:unreadOnly = false OR n.isRead = false)"
          + " AND (n.createdAt < :cursorCreatedAt"
          + " OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId))"
          + " ORDER BY n.createdAt DESC, n.id DESC")
  List<DashboardNotification> findPageAfter(
      @Param("userId") Long userId,
      @Param("unreadOnly") boolean unreadOnly,
      @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
      @Param("cursorId") Long cursorId,
      Pageable pageable);

  long countByUserId(Long userId);

  // Count unread notifications for a user
  @Query(
      "SELECT COUNT(n) FROM DashboardNotification n WHERE n.user.id = :userId AND n.isRead = false")
  Long countUnreadByUserId(@Param("userId") Long userId);

  // Find notifications by user and type
  List<DashboardNotification> findByUserIdAndTypeOrderByCreatedAtDesc(
      Long userId, com.rumantra.notification.domain.NotificationType type);
}
