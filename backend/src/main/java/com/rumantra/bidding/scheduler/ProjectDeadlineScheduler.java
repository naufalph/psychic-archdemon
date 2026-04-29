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
}
