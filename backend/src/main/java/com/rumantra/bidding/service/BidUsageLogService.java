package com.rumantra.bidding.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.architect.domain.Architect;
import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidUsageAction;
import com.rumantra.bidding.domain.BidUsageLog;
import com.rumantra.bidding.repository.BidUsageLogRepository;

@Service
public class BidUsageLogService {

  @Autowired private BidUsageLogRepository bidUsageLogRepository;

  @Transactional
  public void logBidPlaced(Architect architect, Bid bid, Integer quotaAfter) {
    BidUsageLog log =
        BidUsageLog.builder()
            .architect(architect)
            .bid(bid)
            .action(BidUsageAction.BID_PLACED)
            .quotaChange(-1)
            .quotaAfter(quotaAfter)
            .description(
                String.format(
                    "Bid placed on project #%d - %d bids remaining",
                    bid.getProject().getId(), quotaAfter))
            .timestamp(LocalDateTime.now())
            .build();

    bidUsageLogRepository.save(log);
  }

  @Transactional
  public void logBidRefunded(Architect architect, Bid bid, String reason, Integer quotaAfter) {
    BidUsageLog log =
        BidUsageLog.builder()
            .architect(architect)
            .bid(bid)
            .action(BidUsageAction.BID_REFUNDED)
            .quotaChange(+1)
            .quotaAfter(quotaAfter)
            .description(String.format("Bid refunded: %s - %d bids remaining", reason, quotaAfter))
            .timestamp(LocalDateTime.now())
            .build();

    bidUsageLogRepository.save(log);
  }

  //  @Transactional
  //  public void logQuotaReset(Architect architect, BidQuota quota) {
  //    BidUsageLog log =
  //        BidUsageLog.builder()
  //            .architect(architect)
  //            .bid(null)
  //            .action(BidUsageAction.QUOTA_RESET)
  //            .quotaChange(quota.getTokensRemaining())
  //            .quotaAfter(0)
  //            .description(
  //                String.format(
  //                    "%s quota reset - %d bids restored",
  //                    quota.getResetInterval(), quota.getTotalBidsAllowed()))
  //            .timestamp(LocalDateTime.now())
  //            .build();
  //
  //    bidUsageLogRepository.save(log);
  //  }

  @Transactional
  public void logTokenAllocation(Architect architect, Integer tokensAllocated) {
    BidUsageLog log =
        BidUsageLog.builder()
            .architect(architect)
            .bid(null)
            .action(BidUsageAction.TOKEN_ALLOCATED)
            .quotaChange(tokensAllocated)
            .quotaAfter(tokensAllocated)
            .description(String.format("%d bid tokens allocated", tokensAllocated))
            .timestamp(LocalDateTime.now())
            .build();

    bidUsageLogRepository.save(log);
  }

  @Transactional
  public void logQuotaUpgraded(Architect architect, Integer quotaAfter) {
    BidUsageLog log =
        BidUsageLog.builder()
            .architect(architect)
            .bid(null)
            .action(BidUsageAction.QUOTA_UPGRADED)
            .quotaChange(quotaAfter)
            .quotaAfter(quotaAfter)
            .description(String.format("Upgraded to BASIC: %d bid tokens allocated", quotaAfter))
            .timestamp(LocalDateTime.now())
            .build();

    bidUsageLogRepository.save(log);
  }

  @Transactional
  public void logQuotaDowngraded(Architect architect, Integer quotaAfter) {
    BidUsageLog log =
        BidUsageLog.builder()
            .architect(architect)
            .bid(null)
            .action(BidUsageAction.QUOTA_DOWNGRADED)
            .quotaChange(-quotaAfter)
            .quotaAfter(quotaAfter)
            .description(String.format("Downgraded to FREE: quota set to %d tokens", quotaAfter))
            .timestamp(LocalDateTime.now())
            .build();

    bidUsageLogRepository.save(log);
  }

  @Transactional
  public void logTokenPurchase(Architect architect, Integer quantity, BigDecimal amount) {
    BidUsageLog log =
        BidUsageLog.builder()
            .architect(architect)
            .bid(null)
            .action(BidUsageAction.TOKEN_PURCHASED)
            .quotaChange(quantity)
            .quotaAfter(quantity)
            .description(String.format("Purchased %d bid tokens for IDR %,.0f", quantity, amount))
            .timestamp(LocalDateTime.now())
            .build();

    bidUsageLogRepository.save(log);
  }
}
