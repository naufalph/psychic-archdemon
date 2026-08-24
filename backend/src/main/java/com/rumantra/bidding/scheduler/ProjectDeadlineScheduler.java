package com.rumantra.bidding.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidRepository;
import com.rumantra.client.domain.Project;
import com.rumantra.client.domain.ProjectStatus;
import com.rumantra.client.repository.ProjectRepository;
import com.rumantra.client.service.ProjectService;
import com.rumantra.notification.domain.NotificationType;
import com.rumantra.notification.service.DashboardNotificationService;
import com.rumantra.user.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectDeadlineScheduler {

  private final ProjectRepository projectRepository;
  private final BidRepository bidRepository;
  private final ProjectService projectService;
  private final DashboardNotificationService notificationService;
  private final EmailService emailService;

  @Scheduled(cron = "0 0 1 * * *")
  public void processDeadlines() {
    LocalDateTime now = LocalDateTime.now();
    LocalDate today = now.toLocalDate();
    log.info("Running project deadline scheduler at {}", now);
    closeExpiredProjects(now);
    sendDeadlineReminders(today);
    sendNegotiationReminders(today);
    expireNegotiations(now);
  }

  private void closeExpiredProjects(LocalDateTime now) {
    List<Project> expiredProjects =
        projectRepository.findOpenProjectsForClosure(ProjectStatus.OPEN, now);

    log.info("Found {} projects past bidding deadline", expiredProjects.size());

    for (Project project : expiredProjects) {
      try {
        // Load pending bids with architect+user eager before the close transaction commits
        List<Bid> pendingBids =
            bidRepository.findPendingBidsWithArchitect(project.getId(), BidStatus.PENDING);

        projectService.closeExpiredProject(project.getId());

        String clientEmail = project.getClient().getUser().getEmail();
        Long clientUserId = project.getClient().getUser().getId();
        String title = project.getTitle();

        notificationService.createNotification(
            clientUserId,
            NotificationType.PROJECT_CLOSED_NO_WINNER,
            "Bidding Closed",
            String.format("Bidding for \"%s\" has ended with no winner selected.", title),
            null,
            null,
            "PROJECT",
            project.getId());

        emailService.sendProjectClosedEmail(clientEmail, title, true);

        for (Bid bid : pendingBids) {
          Long architectUserId = bid.getArchitect().getUser().getId();
          String architectEmail = bid.getArchitect().getUser().getEmail();

          notificationService.createNotification(
              architectUserId,
              NotificationType.PROJECT_CLOSED_NO_WINNER,
              "Bidding Closed — Token Refunded",
              String.format(
                  "Bidding for \"%s\" has ended. Your bid token has been refunded.", title),
              null,
              null,
              "PROJECT",
              project.getId());

          emailService.sendProjectClosedEmail(architectEmail, title, false);
        }

        log.info(
            "Closed project {} and notified client + {} architects",
            project.getId(),
            pendingBids.size());
      } catch (Exception e) {
        log.error("Failed to process closure for project {}", project.getId(), e);
      }
    }
  }

  private void sendDeadlineReminders(LocalDate today) {
    for (int daysLeft = 1; daysLeft <= 5; daysLeft++) {
      LocalDateTime from = today.plusDays(daysLeft).atStartOfDay();
      LocalDateTime to = today.plusDays(daysLeft + 1).atStartOfDay();
      List<Project> projects =
          projectRepository.findOpenProjectsForReminder(ProjectStatus.OPEN, from, to);

      for (Project project : projects) {
        try {
          Long clientUserId = project.getClient().getUser().getId();
          String clientEmail = project.getClient().getUser().getEmail();
          String title = project.getTitle();
          int days = daysLeft;

          notificationService.createNotification(
              clientUserId,
              NotificationType.BIDDING_DEADLINE_REMINDER,
              "Bidding Deadline Reminder",
              String.format(
                  "The bidding deadline for \"%s\" is in %d day%s.",
                  title, days, days == 1 ? "" : "s"),
              null,
              null,
              "PROJECT",
              project.getId());

          emailService.sendDeadlineReminderEmail(clientEmail, title, days);

          log.info(
              "Sent D-{} reminder for project {} to client user {}",
              days,
              project.getId(),
              clientUserId);
        } catch (Exception e) {
          log.error("Failed to send D-{} reminder for project {}", daysLeft, project.getId(), e);
        }
      }
    }
  }

  private void sendNegotiationReminders(LocalDate today) {
    for (int daysLeft = 3; daysLeft >= 1; daysLeft--) {
      int daysElapsed = 7 - daysLeft;
      LocalDateTime from = today.minusDays(daysElapsed).atStartOfDay();
      LocalDateTime to = today.minusDays(daysElapsed - 1).atStartOfDay();
      List<Bid> acceptedBids =
          bidRepository.findAcceptedBidsForNegotiationReminder(
              BidStatus.ACCEPTED, ProjectStatus.NEGOTIATION, from, to);

      for (Bid bid : acceptedBids) {
        try {
          Project project = bid.getProject();
          Long clientUserId = project.getClient().getUser().getId();
          String clientEmail = project.getClient().getUser().getEmail();
          Long architectUserId = bid.getArchitect().getUser().getId();
          String architectEmail = bid.getArchitect().getUser().getEmail();
          String title = project.getTitle();
          int days = daysLeft;

          String message =
              String.format(
                  "The negotiation window for \"%s\" closes in %d day%s. Please confirm the terms.",
                  title, days, days == 1 ? "" : "s");

          notificationService.createNotification(
              clientUserId,
              NotificationType.NEGOTIATION_DEADLINE_REMINDER,
              "Negotiation Deadline Reminder",
              message,
              null,
              null,
              "PROJECT",
              project.getId());

          notificationService.createNotification(
              architectUserId,
              NotificationType.NEGOTIATION_DEADLINE_REMINDER,
              "Negotiation Deadline Reminder",
              message,
              null,
              null,
              "PROJECT",
              project.getId());

          emailService.sendNegotiationReminderEmail(
              clientEmail, title, days, project.getId(), true);
          emailService.sendNegotiationReminderEmail(
              architectEmail, title, days, project.getId(), false);

          log.info(
              "Sent negotiation D-{} reminder for project {} to client {} and architect {}",
              days,
              project.getId(),
              clientUserId,
              architectUserId);
        } catch (Exception e) {
          log.error("Failed to send negotiation reminder for bid {}", bid.getId(), e);
        }
      }
    }
  }

  private void expireNegotiations(LocalDateTime now) {
    LocalDateTime threshold = now.minusDays(7);
    List<Bid> expiredBids =
        bidRepository.findAcceptedBidsForNegotiationExpiry(
            BidStatus.ACCEPTED, ProjectStatus.NEGOTIATION, threshold);

    log.info("Found {} negotiations past the 7-day window", expiredBids.size());

    for (Bid bid : expiredBids) {
      try {
        Project project = bid.getProject();
        project.setStatus(ProjectStatus.NEGOTIATION_EXPIRED);
        projectRepository.save(project);

        Long clientUserId = project.getClient().getUser().getId();
        String clientEmail = project.getClient().getUser().getEmail();
        Long architectUserId = bid.getArchitect().getUser().getId();
        String architectEmail = bid.getArchitect().getUser().getEmail();
        String title = project.getTitle();

        String message =
            String.format(
                "The negotiation window for \"%s\" has closed without both parties confirming."
                    + " This project is now under admin review.",
                title);

        notificationService.createNotification(
            clientUserId,
            NotificationType.NEGOTIATION_EXPIRED,
            "Negotiation Window Closed",
            message,
            null,
            null,
            "PROJECT",
            project.getId());

        notificationService.createNotification(
            architectUserId,
            NotificationType.NEGOTIATION_EXPIRED,
            "Negotiation Window Closed",
            message,
            null,
            null,
            "PROJECT",
            project.getId());

        emailService.sendNegotiationExpiredEmail(clientEmail, title, true);
        emailService.sendNegotiationExpiredEmail(architectEmail, title, false);

        log.info(
            "Expired negotiation for project {} (bidId={}), now awaiting admin review",
            project.getId(),
            bid.getId());
      } catch (Exception e) {
        log.error("Failed to expire negotiation for bid {}", bid.getId(), e);
      }
    }
  }
}
