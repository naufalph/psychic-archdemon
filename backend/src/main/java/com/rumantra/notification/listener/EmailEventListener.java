package com.rumantra.notification.listener;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.rumantra.bidding.event.BidSubmittedEvent;
import com.rumantra.notification.event.ProjectValidatedEvent;
import com.rumantra.shared.email.EmailTemplateService;
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
  private final EmailTemplateService templateService;
  private final UserRepository userRepository;

  @Value("${app.frontend.url:http://localhost:3001}")
  private String frontendUrl;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleProjectValidated(ProjectValidatedEvent event) {
    try {
      User client =
          userRepository
              .findById(event.getClientUserId())
              .orElseThrow(
                  () -> new RuntimeException("Client user not found: " + event.getClientUserId()));

      String firstName = client.getFirstName() != null ? client.getFirstName() : "there";

      String subject;
      String html;

      if (event.getIsValid()) {
        subject = "Project Approved: " + event.getProjectTitle();
        html =
            templateService.render(
                "project-approved",
                Map.of(
                    "FIRST_NAME", firstName,
                    "PROJECT_TITLE", event.getProjectTitle(),
                    "FRONTEND_URL", frontendUrl));
      } else {
        subject = "Project Needs Changes: " + event.getProjectTitle();

        String validationNotesHtml = "";
        if (event.getValidationNotes() != null && !event.getValidationNotes().isBlank()) {
          validationNotesHtml =
              "<div style=\"background-color:#fff7ed;border-left:4px solid #f97316;border-radius:4px;padding:16px 20px;margin-bottom:24px;\">"
                  + "<p style=\"margin:0 0 6px 0;font-size:12px;font-weight:600;color:#c2410c;text-transform:uppercase;letter-spacing:0.5px;\">Reviewer Notes</p>"
                  + "<p style=\"margin:0;font-size:14px;color:#374151;line-height:1.6;\">"
                  + event.getValidationNotes()
                  + "</p>"
                  + "</div>";
        }

        Map<String, String> vars = new HashMap<>();
        vars.put("FIRST_NAME", firstName);
        vars.put("PROJECT_TITLE", event.getProjectTitle());
        vars.put("VALIDATION_NOTES", validationNotesHtml);
        vars.put("FRONTEND_URL", frontendUrl);
        html = templateService.render("project-needs-changes", vars);
      }

      emailService.sendProjectValidationEmail(client.getEmail(), subject, html);

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
    }
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleBidSubmitted(BidSubmittedEvent event) {
    try {
      User client =
          userRepository
              .findById(event.getClientUserId())
              .orElseThrow(
                  () -> new RuntimeException("Client user not found: " + event.getClientUserId()));

      String firstName = client.getFirstName() != null ? client.getFirstName() : "there";
      String subject = "New Bid for Your Project: " + event.getProjectTitle();
      String html =
          templateService.render(
              "new-bid",
              Map.of(
                  "FIRST_NAME",
                  firstName,
                  "ARCHITECT_NAME",
                  event.getArchitectName(),
                  "PROJECT_TITLE",
                  event.getProjectTitle(),
                  "FRONTEND_URL",
                  frontendUrl));

      emailService.sendBidNotificationEmail(client.getEmail(), subject, html);

      log.info(
          "Bid notification email sent to {} for project {}: bidId={}",
          client.getEmail(),
          event.getProjectId(),
          event.getBidId());

    } catch (Exception e) {
      log.error(
          "Failed to send bid notification email for bid {}: {}",
          event.getBidId(),
          e.getMessage(),
          e);
    }
  }
}
