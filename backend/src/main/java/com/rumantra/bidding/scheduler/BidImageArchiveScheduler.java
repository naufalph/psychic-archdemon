package com.rumantra.bidding.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rumantra.bidding.service.BidImageArchiveService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidImageArchiveScheduler {

  private final BidImageArchiveService bidImageArchiveService;

  @Value("${bid.image.archive.enabled:true}")
  private boolean enabled;

  /**
   * Weekly, offset from ProjectDeadlineScheduler's daily 01:00 run — Spring's default scheduler
   * pool is single-threaded, so overlapping jobs would block each other.
   */
  @Scheduled(cron = "${bid.image.archive.cron:0 0 3 * * SUN}", zone = "Asia/Jakarta")
  public void archiveExpiredBidImages() {
    if (!enabled) {
      log.debug("Bid image archiving is disabled, skipping run");
      return;
    }

    log.info("Running bid image archive job");
    try {
      bidImageArchiveService.archiveExpiredImages();
    } catch (Exception e) {
      log.error("Bid image archive job failed: {}", e.getMessage(), e);
    }
  }
}
