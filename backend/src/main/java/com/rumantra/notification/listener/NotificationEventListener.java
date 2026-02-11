package com.rumantra.notification.listener;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.rumantra.notification.domain.NotificationType;
import com.rumantra.notification.event.ProjectValidatedEvent;
import com.rumantra.notification.service.DashboardNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final DashboardNotificationService dashboardNotificationService;

  /**
   * Handle ProjectValidatedEvent by creating a dashboard notification.
   * Uses @TransactionalEventListener to ensure notification is only created after the project
   * validation transaction commits successfully.
   *
   * @param event The project validated event
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleProjectValidated(ProjectValidatedEvent event) {
    try {
      String title = event.getIsValid() ? "Project Approved" : "Project Needs Changes";
      String message = buildNotificationMessage(event);

      dashboardNotificationService.createNotification(
          event.getClientId(),
          NotificationType.PROJECT_VALIDATED,
          title,
          message,
          null,
          null,
          "PROJECT",
          event.getProjectId());

      log.info(
          "Dashboard notification created for project validation: projectId={}, isValid={}, clientId={}",
          event.getProjectId(),
          event.getIsValid(),
          event.getClientId());

    } catch (Exception e) {
      log.error(
          "Failed to create dashboard notification for project {}: {}",
          event.getProjectId(),
          e.getMessage(),
          e);
    }
  }

  private String buildNotificationMessage(ProjectValidatedEvent event) {
    if (event.getIsValid()) {
      return String.format(
          "Your project '%s' has been approved and is now visible to architects!",
          event.getProjectTitle());
    } else {
      String baseMessage =
          String.format("Your project '%s' requires changes.", event.getProjectTitle());

      if (event.getValidationNotes() != null && !event.getValidationNotes().isBlank()) {
        return baseMessage + " Reason: " + event.getValidationNotes();
      }
      return baseMessage + " Please review and update your project.";
    }
  }
}
