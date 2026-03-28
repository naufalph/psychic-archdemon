package com.rumantra.subscription.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.integration.xendit.XenditService;
import com.rumantra.integration.xendit.dto.XenditWebhookEvent;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.subscription.domain.Subscription;
import com.rumantra.subscription.service.SubscriptionService;
import com.rumantra.subscription.service.SubscriptionUpgradeResponse;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rmtr/subscriptions")
@Slf4j
public class SubscriptionController {

  @Autowired private SubscriptionService subscriptionService;

  @Autowired private ArchitectRepository architectRepository;

  @Autowired private XenditService xenditService;

  @PostMapping("/upgrade")
  public ResponseEntity<ApiResponse<SubscriptionUpgradeResponse>> upgradeSubscription() {
    Long userId = SecurityUtils.getCurrentUserId();
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new BusinessException(ExceptionConstants.UNAUTHORIZED_ARCHITECT_ACCESS));

    SubscriptionUpgradeResponse response = subscriptionService.initiateUpgrade(architect);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.<SubscriptionUpgradeResponse>builder()
                .success(true)
                .message("Subscription upgrade initiated")
                .data(response)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build());
  }

  @GetMapping("/status")
  public ResponseEntity<ApiResponse<Subscription>> getSubscriptionStatus() {
    Long userId = SecurityUtils.getCurrentUserId();
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new BusinessException(ExceptionConstants.UNAUTHORIZED_ARCHITECT_ACCESS));

    Subscription subscription = subscriptionService.getActiveSubscription(architect.getId());

    return ResponseEntity.ok(
        ApiResponse.<Subscription>builder()
            .success(true)
            .message("Subscription status retrieved")
            .data(subscription)
            .timestamp(java.time.LocalDateTime.now().toString())
            .build());
  }

  @PostMapping("/cancel")
  public ResponseEntity<ApiResponse<Void>> cancelSubscription() {
    Long userId = SecurityUtils.getCurrentUserId();
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new BusinessException(ExceptionConstants.UNAUTHORIZED_ARCHITECT_ACCESS));

    subscriptionService.cancelSubscription(architect.getId());

    return ResponseEntity.ok(
        ApiResponse.<Void>builder()
            .success(true)
            .message("Subscription cancelled successfully")
            .timestamp(java.time.LocalDateTime.now().toString())
            .build());
  }

  @PostMapping("/webhook")
  public ResponseEntity<Void> handleXenditWebhook(
      @RequestHeader("x-callback-token") String callbackToken, @RequestBody String payload) {

    if (!xenditService.verifyWebhookToken(callbackToken)) {
      log.warn("Invalid webhook token received");
      return ResponseEntity.status(403).build();
    }

    XenditWebhookEvent event = xenditService.parseWebhook(payload);

    switch (event.getStatus()) {
      case "ACTIVE":
        subscriptionService.handlePaymentSucceeded(event);
        break;
      case "FAILED":
        subscriptionService.handlePaymentFailed(event);
        break;
      case "STOPPED":
        subscriptionService.handleSubscriptionStopped(event);
        break;
      default:
        log.info("Unhandled webhook status: {}", event.getStatus());
    }

    return ResponseEntity.ok().build();
  }
}
