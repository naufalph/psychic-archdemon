package com.rumantra.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${app.frontend.url:http://localhost:3000}")
  private String frontendUrl;

  @Value("${spring.mail.username}")
  private String fromEmail;

  public void sendVerificationEmail(String toEmail, String token) {
    try {
      String verificationLink = frontendUrl + "/verify-email?token=" + token;

      String subject = "Verify Your Rumantra Account";
      String body = buildVerificationEmailBody(verificationLink);

      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(toEmail);
      message.setSubject(subject);
      message.setText(body);

      mailSender.send(message);
      log.info("Verification email sent successfully to: {}", toEmail);

    } catch (Exception e) {
      log.error("Failed to send verification email to: {}", toEmail, e);
      throw new IllegalStateException("Failed to send verification email: " + e.getMessage());
    }
  }

  public void sendWelcomeEmail(String toEmail, String firstName) {
    try {
      String subject = "Welcome to Rumantra!";
      String body = buildWelcomeEmailBody(firstName);

      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(toEmail);
      message.setSubject(subject);
      message.setText(body);

      mailSender.send(message);
      log.info("Welcome email sent successfully to: {}", toEmail);

    } catch (Exception e) {
      log.error("Failed to send welcome email to: {}", toEmail, e);
      // Don't throw exception for welcome email failure
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
}
