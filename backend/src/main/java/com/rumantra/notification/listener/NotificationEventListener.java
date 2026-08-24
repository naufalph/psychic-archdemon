package com.rumantra.notification.listener;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumantra.admin.event.NegotiationDisputeResolvedEvent;
import com.rumantra.bidding.event.BidAcceptedEvent;
import com.rumantra.bidding.event.BidSubmittedEvent;
import com.rumantra.chat.event.SupportRequestedEvent;
import com.rumantra.notification.domain.NotificationType;
import com.rumantra.notification.event.ProjectValidatedEvent;
import com.rumantra.notification.service.DashboardNotificationService;
import com.rumantra.project.event.RevisionRequestedEvent;
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
  private final ObjectMapper objectMapper;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handleProjectValidated(ProjectValidatedEvent event) {
    try {
      String title = event.getIsValid() ? "Project Approved" : "Project Needs Changes";
      String message = buildNotificationMessage(event);

      dashboardNotificationService.createNotification(
          event.getClientUserId(),
          NotificationType.PROJECT_VALIDATED,
          title,
          message,
          null,
          null,
          "PROJECT",
          event.getProjectId());

      log.info(
          "Dashboard notification created for project validation: projectId={}, isValid={}, clientUserId={}",
          event.getProjectId(),
          event.getIsValid(),
          event.getClientUserId());

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
  public void handleBidAccepted(BidAcceptedEvent event) {
    try {
      String messageData =
          objectMapper.writeValueAsString(Map.of("projectName", event.getProjectTitle()));

      dashboardNotificationService.createNotification(
          event.getWinningArchitectUserId(),
          NotificationType.BID_ACCEPTED,
          "Bid Accepted",
          String.format(
              "Your bid on \"%s\" has been accepted! Head to the finalization page to confirm the terms.",
              event.getProjectTitle()),
          "NOTIFICATION_BID_ACCEPTED",
          messageData,
          "PROJECT",
          event.getProjectId());

      for (BidAcceptedEvent.RejectedArchitect rejected : event.getRejectedArchitects()) {
        dashboardNotificationService.createNotification(
            rejected.getUserId(),
            NotificationType.BID_REJECTED,
            "Bid Update",
            String.format("Your bid on \"%s\" was not selected.", event.getProjectTitle()),
            "NOTIFICATION_BID_REJECTED",
            messageData,
            "PROJECT",
            event.getProjectId());
      }

      log.info(
          "Dashboard notifications created for bid acceptance: bidId={}, projectId={}, winnerUserId={}, rejectedCount={}",
          event.getBidId(),
          event.getProjectId(),
          event.getWinningArchitectUserId(),
          event.getRejectedArchitects().size());

    } catch (Exception e) {
      log.error(
          "Failed to create dashboard notifications for bid acceptance {}: {}",
          event.getBidId(),
          e.getMessage(),
          e);
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handleNegotiationDisputeResolved(NegotiationDisputeResolvedEvent event) {
    try {
      boolean clientAbandoned = "CLIENT_ABANDONED".equals(event.getDecision());

      String clientMessage =
          clientAbandoned
              ? String.format(
                  "Your project \"%s\" was cancelled after the negotiation window expired without your confirmation.",
                  event.getProjectTitle())
              : String.format(
                  "Your project \"%s\" was cancelled after the architect did not confirm the terms in time.",
                  event.getProjectTitle());

      String architectMessage =
          clientAbandoned
              ? String.format(
                  "Project \"%s\" was cancelled and your bid token has been refunded.",
                  event.getProjectTitle())
              : String.format(
                  "Project \"%s\" was cancelled because the negotiation window expired without your"
                      + " confirmation. Your bid token was not refunded.",
                  event.getProjectTitle());

      dashboardNotificationService.createNotification(
          event.getClientUserId(),
          NotificationType.NEGOTIATION_DISPUTE_RESOLVED,
          "Project Cancelled",
          clientMessage,
          null,
          null,
          "PROJECT",
          event.getProjectId());

      dashboardNotificationService.createNotification(
          event.getArchitectUserId(),
          NotificationType.NEGOTIATION_DISPUTE_RESOLVED,
          "Project Cancelled",
          architectMessage,
          null,
          null,
          "PROJECT",
          event.getProjectId());

      log.info(
          "Dashboard notifications created for negotiation dispute resolution: projectId={}, decision={}",
          event.getProjectId(),
          event.getDecision());

    } catch (Exception e) {
      log.error(
          "Failed to create dashboard notifications for negotiation dispute resolution {}: {}",
          event.getProjectId(),
          e.getMessage(),
          e);
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handleRevisionRequested(RevisionRequestedEvent event) {
    try {
      String message =
          String.format(
              "Client requested revision %d/%d on phase '%s' of project '%s'.%s",
              event.getRevisionsUsed(),
              event.getMaxRevisions(),
              event.getPhaseTitle(),
              event.getProjectTitle(),
              event.getNotes() != null && !event.getNotes().isBlank()
                  ? " Notes: " + event.getNotes()
                  : "");

      dashboardNotificationService.createNotification(
          event.getArchitectUserId(),
          NotificationType.REVISION_REQUESTED,
          "Revision Requested",
          message,
          null,
          null,
          "PHASE",
          event.getPhaseId());

      log.info(
          "Revision notification sent to architect userId={} for phaseId={}",
          event.getArchitectUserId(),
          event.getPhaseId());
    } catch (Exception e) {
      log.error(
          "Failed to create revision notification for phaseId={}: {}",
          event.getPhaseId(),
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
