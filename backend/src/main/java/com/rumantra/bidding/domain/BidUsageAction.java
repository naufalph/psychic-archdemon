package com.rumantra.bidding.domain;

public enum BidUsageAction {
  BID_PLACED, // Architect placed bid (-1 quota)
  BID_REFUNDED, // Project cancelled (+1 quota)
  QUOTA_RESET, // Scheduled reset
  QUOTA_UPGRADED, // Premium upgrade
  QUOTA_DOWNGRADED, // Back to free tier
  MANUAL_ADJUSTMENT // Admin action
}
