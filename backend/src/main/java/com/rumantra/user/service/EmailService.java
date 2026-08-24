package com.rumantra.user.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.rumantra.shared.email.EmailTemplateService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;
  private final EmailTemplateService templateService;

  @Value("${app.frontend.url:http://localhost:3001}")
  private String frontendUrl;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Async
  public void sendVerificationEmail(String toEmail, String token) {
    long startTime = System.currentTimeMillis();
    log.info("Starting email send to: {}", toEmail);

    try {
      String verificationLink = frontendUrl + "/verify-email?token=" + token;
      String html =
          templateService.render("verification", Map.of("VERIFICATION_LINK", verificationLink));
      sendHtmlEmail(toEmail, "Verify Your Rumantra Account", html);

      long duration = System.currentTimeMillis() - startTime;
      log.info("Verification email sent successfully to: {} ({}ms)", toEmail, duration);
    } catch (Exception e) {
      log.error(
          "Failed to send verification email to: {} after {}ms",
          toEmail,
          System.currentTimeMillis() - startTime,
          e);
    }
  }

  @Async
  public void sendWelcomeEmail(String toEmail, String firstName) {
    long startTime = System.currentTimeMillis();

    try {
      String html =
          templateService.render(
              "welcome",
              Map.of(
                  "FIRST_NAME",
                  firstName != null ? firstName : "there",
                  "FRONTEND_URL",
                  frontendUrl));
      sendHtmlEmail(toEmail, "Welcome to Rumantra!", html);

      long duration = System.currentTimeMillis() - startTime;
      log.info("Welcome email sent successfully to: {} ({}ms)", toEmail, duration);
    } catch (Exception e) {
      log.error(
          "Failed to send welcome email to: {} after {}ms",
          toEmail,
          System.currentTimeMillis() - startTime,
          e);
    }
  }

  @Async
  public void sendProjectValidationEmail(String toEmail, String subject, String htmlBody) {
    long startTime = System.currentTimeMillis();

    try {
      sendHtmlEmail(toEmail, subject, htmlBody);

      long duration = System.currentTimeMillis() - startTime;
      log.info("Project validation email sent successfully to: {} ({}ms)", toEmail, duration);
    } catch (Exception e) {
      log.error(
          "Failed to send project validation email to: {} after {}ms",
          toEmail,
          System.currentTimeMillis() - startTime,
          e);
    }
  }

  @Async
  public void sendDeadlineReminderEmail(String toEmail, String projectTitle, int daysLeft) {
    try {
      String subject =
          String.format(
              "Reminder: Bid deadline in %d day%s — %s",
              daysLeft, daysLeft == 1 ? "" : "s", projectTitle);
      String html =
          templateService.render(
              "deadline-reminder",
              Map.of(
                  "PROJECT_TITLE",
                  projectTitle,
                  "DAYS_LEFT",
                  String.valueOf(daysLeft),
                  "DAYS_SUFFIX",
                  daysLeft == 1 ? "" : "s",
                  "FRONTEND_URL",
                  frontendUrl));
      sendHtmlEmail(toEmail, subject, html);
      log.info("Deadline reminder email ({} days) sent to {}", daysLeft, toEmail);
    } catch (Exception e) {
      log.error("Failed to send deadline reminder email to {}", toEmail, e);
    }
  }

  @Async
  public void sendProjectClosedEmail(String toEmail, String projectTitle, boolean isClient) {
    try {
      String subject = String.format("Bidding closed — %s", projectTitle);
      String templateName = isClient ? "project-closed-client" : "project-closed-architect";
      String html =
          templateService.render(
              templateName,
              Map.of(
                  "PROJECT_TITLE", projectTitle,
                  "FRONTEND_URL", frontendUrl));
      sendHtmlEmail(toEmail, subject, html);
      log.info("Project closed email sent to {} (isClient={})", toEmail, isClient);
    } catch (Exception e) {
      log.error("Failed to send project closed email to {}", toEmail, e);
    }
  }

  @Async
  public void sendBidAcceptedEmail(String toEmail, String projectTitle, Long projectId) {
    try {
      String subject = String.format("You've been selected — %s", projectTitle);
      String html =
          templateService.render(
              "bid-accepted",
              Map.of(
                  "PROJECT_TITLE", projectTitle,
                  "PROJECT_ID", String.valueOf(projectId),
                  "FRONTEND_URL", frontendUrl));
      sendHtmlEmail(toEmail, subject, html);
      log.info("Bid accepted email sent to {}", toEmail);
    } catch (Exception e) {
      log.error("Failed to send bid accepted email to {}", toEmail, e);
    }
  }

  @Async
  public void sendBidRejectedEmail(String toEmail, String projectTitle) {
    try {
      String subject = String.format("Bid update — %s", projectTitle);
      String html =
          templateService.render(
              "bid-rejected",
              Map.of(
                  "PROJECT_TITLE", projectTitle,
                  "FRONTEND_URL", frontendUrl));
      sendHtmlEmail(toEmail, subject, html);
      log.info("Bid rejected email sent to {}", toEmail);
    } catch (Exception e) {
      log.error("Failed to send bid rejected email to {}", toEmail, e);
    }
  }

  @Async
  public void sendNegotiationReminderEmail(
      String toEmail, String projectTitle, int daysLeft, Long projectId, boolean isClient) {
    try {
      String subject =
          String.format(
              "Reminder: %d day%s left to confirm — %s",
              daysLeft, daysLeft == 1 ? "" : "s", projectTitle);
      String ctaPath =
          (isClient ? "/client/projects/" : "/architect/projects/") + projectId + "/finalization";
      String html =
          templateService.render(
              "negotiation-deadline-reminder",
              Map.of(
                  "PROJECT_TITLE", projectTitle,
                  "DAYS_LEFT", String.valueOf(daysLeft),
                  "DAYS_SUFFIX", daysLeft == 1 ? "" : "s",
                  "CTA_PATH", ctaPath,
                  "FRONTEND_URL", frontendUrl));
      sendHtmlEmail(toEmail, subject, html);
      log.info("Negotiation reminder email ({} days) sent to {}", daysLeft, toEmail);
    } catch (Exception e) {
      log.error("Failed to send negotiation reminder email to {}", toEmail, e);
    }
  }

  @Async
  public void sendNegotiationExpiredEmail(String toEmail, String projectTitle, boolean isClient) {
    try {
      String subject = String.format("Negotiation window closed — %s", projectTitle);
      String ctaPath = isClient ? "/client/dashboard" : "/architect/dashboard";
      String html =
          templateService.render(
              "negotiation-expired",
              Map.of(
                  "PROJECT_TITLE", projectTitle,
                  "CTA_PATH", ctaPath,
                  "FRONTEND_URL", frontendUrl));
      sendHtmlEmail(toEmail, subject, html);
      log.info("Negotiation expired email sent to {}", toEmail);
    } catch (Exception e) {
      log.error("Failed to send negotiation expired email to {}", toEmail, e);
    }
  }

  @Async
  public void sendNegotiationResolvedEmail(
      String toEmail, String projectTitle, String outcomeMessage, boolean isClient) {
    try {
      String subject = String.format("Project cancelled — %s", projectTitle);
      String ctaPath = isClient ? "/client/dashboard" : "/architect/dashboard";
      String html =
          templateService.render(
              "negotiation-resolved",
              Map.of(
                  "PROJECT_TITLE", projectTitle,
                  "OUTCOME_MESSAGE", outcomeMessage,
                  "CTA_PATH", ctaPath,
                  "FRONTEND_URL", frontendUrl));
      sendHtmlEmail(toEmail, subject, html);
      log.info("Negotiation resolution email sent to {}", toEmail);
    } catch (Exception e) {
      log.error("Failed to send negotiation resolution email to {}", toEmail, e);
    }
  }

  @Async
  public void sendBidNotificationEmail(String toEmail, String subject, String htmlBody) {
    long startTime = System.currentTimeMillis();

    try {
      sendHtmlEmail(toEmail, subject, htmlBody);

      long duration = System.currentTimeMillis() - startTime;
      log.info("Bid notification email sent successfully to: {} ({}ms)", toEmail, duration);
    } catch (Exception e) {
      log.error(
          "Failed to send bid notification email to: {} after {}ms",
          toEmail,
          System.currentTimeMillis() - startTime,
          e);
    }
  }

  private void sendHtmlEmail(String toEmail, String subject, String htmlBody)
      throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    helper.setFrom(fromEmail);
    helper.setTo(toEmail);
    helper.setSubject(subject);
    helper.setText(htmlBody, true);
    mailSender.send(message);
  }
}
