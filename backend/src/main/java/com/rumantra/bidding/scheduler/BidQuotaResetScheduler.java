package com.rumantra.bidding.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rumantra.bidding.repository.BidQuotaRepository;
import com.rumantra.bidding.service.BidQuotaService;
import com.rumantra.bidding.service.BidUsageLogService;

@Component
public class BidQuotaResetScheduler {

  private static final Logger logger = LoggerFactory.getLogger(BidQuotaResetScheduler.class);

  @Autowired private BidQuotaRepository bidQuotaRepository;

  @Autowired private BidQuotaService bidQuotaService;

  @Autowired private BidUsageLogService bidUsageLogService;

  //  @Scheduled(cron = "0 0 2 * * *") // Every day at 2 AM
  //  @Transactional
  //  public void resetExpiredQuotas() {
  //    logger.info("Starting bid quota reset job");
  //
  //    LocalDateTime now = LocalDateTime.now();
  //    List<BidQuota> quotasToReset = bidQuotaRepository.findByNextResetDateBefore(now);
  //
  //    logger.info("Found {} quotas to reset", quotasToReset.size());
  //
  //    for (BidQuota quota : quotasToReset) {
  //      try {
  //        bidQuotaService.resetQuota(quota);
  //        bidUsageLogService.logQuotaReset(quota.getArchitect(), quota);
  //        logger.info(
  //            "Reset quota for architect ID {} - tier: {}, bids restored: {}",
  //            quota.getArchitect().getId(),
  //            quota.getTier(),
  //            quota.getTotalBidsAllowed());
  //      } catch (Exception e) {
  //        logger.error("Failed to reset quota for architect ID: " + quota.getArchitect().getId(),
  // e);
  //      }
  //    }
  //
  //    logger.info("Bid quota reset job completed");
  //  }
}
