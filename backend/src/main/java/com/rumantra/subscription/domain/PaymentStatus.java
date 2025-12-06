package com.rumantra.subscription.domain;

public enum PaymentStatus {
  ACTIVE, // Subscription is paid and working
  EXPIRED, // Subscription period ended
  CANCELLED, // User cancelled subscription
  PENDING // Payment being processed
}
