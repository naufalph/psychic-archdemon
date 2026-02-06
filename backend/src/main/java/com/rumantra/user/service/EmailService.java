package com.rumantra.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

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

      String subject = "Verify Your Rumantra Account";
      String body = buildVerificationEmailBody(verificationLink);

      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(toEmail);
      message.setSubject(subject);
      message.setText(body);

      long smtpStartTime = System.currentTimeMillis();
      mailSender.send(message);
      long smtpDuration = System.currentTimeMillis() - smtpStartTime;

      long totalDuration = System.currentTimeMillis() - startTime;
      log.info(
          "Verification email sent successfully to: {} (SMTP: {}ms, Total: {}ms)",
          toEmail,
          smtpDuration,
          totalDuration);

    } catch (Exception e) {
      long totalDuration = System.currentTimeMillis() - startTime;
      log.error("Failed to send verification email to: {} after {}ms", toEmail, totalDuration, e);
      // Don't throw exception in async method - it won't be caught by caller
    }
  }

  @Async
  public void sendWelcomeEmail(String toEmail, String firstName) {
    long startTime = System.currentTimeMillis();

    try {
      String subject = "Welcome to Rumantra!";
      String body = buildWelcomeEmailBody(firstName);

      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(toEmail);
      message.setSubject(subject);
      message.setText(body);

      long smtpStartTime = System.currentTimeMillis();
      mailSender.send(message);
      long smtpDuration = System.currentTimeMillis() - smtpStartTime;

      long totalDuration = System.currentTimeMillis() - startTime;
      log.info(
          "Welcome email sent successfully to: {} (SMTP: {}ms, Total: {}ms)",
          toEmail,
          smtpDuration,
          totalDuration);

    } catch (Exception e) {
      long totalDuration = System.currentTimeMillis() - startTime;
      log.error("Failed to send welcome email to: {} after {}ms", toEmail, totalDuration, e);
    }
  }

  private String buildVerificationEmailBody(String verificationLink) {
    return String.format(
        "Welcome to Rumantra!\n\n"
            + "Thank you for registering with us. To complete your registration and activate your account, "
            + "please verify your email address by clicking the link below:\n\n"
            + "%s\n\n"
            + "This verification link will expire in 24 hours for security reasons.\n\n"
            + "If you didn't create an account with Rumantra, please ignore this email.\n\n"
            + "Best regards,\n"
            + "The Rumantra Team\n\n"
            + "---\n"
            + "This is an automated message. Please do not reply to this email.",
        verificationLink);
  }

  private String buildWelcomeEmailBody(String firstName) {
    return String.format(
        "Hello %s,\n\n"
            + "Welcome to Rumantra! Your email has been successfully verified and your account is now active.\n\n"
            + "You can now:\n"
            + "• Browse and connect with talented architects\n"
            + "• Post your architectural projects\n"
            + "• Manage your profile and portfolio\n\n"
            + "Get started by visiting: %s\n\n"
            + "If you have any questions, feel free to contact our support team.\n\n"
            + "Best regards,\n"
            + "The Rumantra Team",
        firstName != null ? firstName : "there", frontendUrl);
  }

  @Async
  public void sendProjectValidationEmail(String toEmail, String subject, String message) {
    long startTime = System.currentTimeMillis();

    try {
      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setFrom(fromEmail);
      mailMessage.setTo(toEmail);
      mailMessage.setSubject(subject);
      mailMessage.setText(message);

      long smtpStartTime = System.currentTimeMillis();
      mailSender.send(mailMessage);
      long smtpDuration = System.currentTimeMillis() - smtpStartTime;

      long totalDuration = System.currentTimeMillis() - startTime;
      log.info(
          "Project validation email sent successfully to: {} (SMTP: {}ms, Total: {}ms)",
          toEmail,
          smtpDuration,
          totalDuration);

    } catch (Exception e) {
      long totalDuration = System.currentTimeMillis() - startTime;
      log.error(
          "Failed to send project validation email to: {} after {}ms", toEmail, totalDuration, e);
    }
  }
}
