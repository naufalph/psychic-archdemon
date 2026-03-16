package com.rumantra.notification.listener;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.rumantra.bidding.event.BidSubmittedEvent;
import com.rumantra.chat.event.SupportRequestedEvent;
import com.rumantra.notification.domain.NotificationType;
import com.rumantra.notification.event.ProjectValidatedEvent;
import com.rumantra.notification.service.DashboardNotificationService;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final DashboardNotificationService dashboardNotificationService;
  private final UserRepository userRepository;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
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

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handleBidSubmitted(BidSubmittedEvent event) {
    try {
      String title = "New Bid Received";
      String message =
          String.format(
              "Architect %s has submitted a bid for your project '%s'. Review the proposal and portfolio in your dashboard.",
              event.getArchitectName(), event.getProjectTitle());

      dashboardNotificationService.createNotification(
          event.getClientUserId(),
          NotificationType.BID_RECEIVED,
          title,
          message,
          null,
          null,
          "PROJECT",
          event.getProjectId());

      log.info(
          "Dashboard notification created for bid submission: bidId={}, projectId={}, clientUserId={}",
          event.getBidId(),
          event.getProjectId(),
          event.getClientUserId());

    } catch (Exception e) {
      log.error(
          "Failed to create dashboard notification for bid {}: {}",
          event.getBidId(),
          e.getMessage(),
          e);
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handleSupportRequested(SupportRequestedEvent event) {
    try {
      List<User> superusers = userRepository.findAllByIsSuperuserTrue();

      for (User superuser : superusers) {
        dashboardNotificationService.createNotification(
            superuser.getId(),
            NotificationType.SUPPORT_REQUESTED,
            "Support Request",
            "A user has requested IT support. Conversation ID: " + event.getConversationId(),
            null,
            null,
            "SUPPORT_CONVERSATION",
            event.getConversationId());
      }

      log.info(
          "Support request notifications sent to {} superusers for conversationId={}",
          superusers.size(),
          event.getConversationId());

    } catch (Exception e) {
      log.error(
          "Failed to create support request notifications for conversationId={}: {}",
          event.getConversationId(),
          e.getMessage(),
          e);
    }
  }
}
