package com.rumantra.notification.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.rumantra.notification.event.ProjectValidatedEvent;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;
import com.rumantra.user.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventListener {

  private final EmailService emailService;
  private final UserRepository userRepository;

  /**
   * Handle ProjectValidatedEvent by sending an email notification.
   * Runs asynchronously to avoid blocking the main thread.
   * Uses @TransactionalEventListener to ensure email is only sent after the project
   * validation transaction commits successfully.
   *
   * @param event The project validated event
   */
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleProjectValidated(ProjectValidatedEvent event) {
    try {
      User client =
          userRepository
              .findById(event.getClientId())
              .orElseThrow(
                  () -> new RuntimeException("Client user not found: " + event.getClientId()));

      String subject =
          event.getIsValid()
              ? "Project Approved: " + event.getProjectTitle()
              : "Project Needs Changes: " + event.getProjectTitle();

      String message =
          event.getIsValid()
              ? buildApprovedEmailBody(client.getFirstName(), event.getProjectTitle())
              : buildNeedsChangesEmailBody(client.getFirstName(), event.getProjectTitle());

      emailService.sendProjectValidationEmail(client.getEmail(), subject, message);

      log.info(
          "Validation email sent to {} for project {}: isValid={}",
          client.getEmail(),
          event.getProjectId(),
          event.getIsValid());

    } catch (Exception e) {
      log.error(
          "Failed to send validation email for project {}: {}",
          event.getProjectId(),
          e.getMessage(),
          e);
      // Don't throw - email failure shouldn't affect other listeners
    }
  }

  private String buildApprovedEmailBody(String firstName, String projectTitle) {
    return String.format(
        "Hello %s,\n\n"
            + "Great news! Your project '%s' has been approved and is now visible to architects on Rumantra.\n\n"
            + "Architects can now view your project and submit bids. You'll receive notifications when bids are submitted.\n\n"
            + "Next steps:\n"
            + "• Review incoming bids from architects\n"
            + "• Compare proposals and portfolios\n"
            + "• Select the best architect for your project\n\n"
            + "View your project and manage bids in your dashboard.\n\n"
            + "Best regards,\n"
            + "The Rumantra Team",
        firstName != null ? firstName : "there",
        projectTitle);
  }

  private String buildNeedsChangesEmailBody(String firstName, String projectTitle) {
    return String.format(
        "Hello %s,\n\n"
            + "Your project '%s' has been reviewed and requires some changes before it can be published.\n\n"
            + "Please review your project details and make the necessary updates. Common reasons for changes:\n"
            + "• Incomplete project information\n"
            + "• Missing required documents\n"
            + "• Budget or scope clarifications needed\n\n"
            + "Once you've updated your project, it will be reviewed again.\n\n"
            + "If you have questions, please contact our support team.\n\n"
            + "Best regards,\n"
            + "The Rumantra Team",
        firstName != null ? firstName : "there",
        projectTitle);
  }
}
