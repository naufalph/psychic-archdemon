package com.rumantra.subscription.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscriptionUpgradeResponse {
  private String paymentLink;
  private String mobilePaymentLink;
  private String status;
}
