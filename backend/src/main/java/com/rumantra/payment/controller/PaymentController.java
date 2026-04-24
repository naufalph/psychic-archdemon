package com.rumantra.payment.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.integration.xendit.XenditService;
import com.rumantra.integration.xendit.dto.XenditInvoiceWebhook;
import com.rumantra.payment.dto.PhasePaymentInitiateResponse;
import com.rumantra.payment.dto.PhasePaymentResponse;
import com.rumantra.payment.service.PaymentService;
import com.rumantra.payment.service.TokenPurchaseService;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rmtr/payments")
@Slf4j
public class PaymentController {

  @Autowired private PaymentService paymentService;

  @Autowired private TokenPurchaseService tokenPurchaseService;

  @Autowired private XenditService xenditService;

  @GetMapping("/projects/{projectId}")
  public ResponseEntity<ApiResponse<List<PhasePaymentResponse>>> getProjectPhasePayments(
      @PathVariable Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();
    List<PhasePaymentResponse> phases = paymentService.getProjectPhasePayments(projectId, userId);
    return ResponseEntity.ok(
        ApiResponse.<List<PhasePaymentResponse>>builder()
            .success(true)
            .message("Phase payments retrieved")
            .data(phases)
            .timestamp(java.time.LocalDateTime.now().toString())
            .build());
  }

  @PostMapping("/phases/{phaseId}")
  public ResponseEntity<ApiResponse<PhasePaymentInitiateResponse>> initiatePhasePayment(
      @PathVariable Long phaseId) {
    Long userId = SecurityUtils.getCurrentUserId();
    PhasePaymentInitiateResponse response = paymentService.initiatePhasePayment(phaseId, userId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.<PhasePaymentInitiateResponse>builder()
                .success(true)
                .message("Phase payment initiated")
                .data(response)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build());
  }

  /**
   * Unified Xendit invoice webhook handler. Routes by external_id prefix: - "phase_payment_*" →
   * PhasePaymentService - "token_purchase_*" → TokenPurchaseService
   */
  @PostMapping("/webhook/invoice")
  public ResponseEntity<Void> handleInvoiceWebhook(
      @RequestHeader("X-CALLBACK-TOKEN") String callbackToken, @RequestBody String payload) {

    if (!xenditService.verifyWebhookToken(callbackToken)) {
      log.warn("Invalid webhook token received for invoice");
      return ResponseEntity.status(403).build();
    }

    XenditInvoiceWebhook webhook = xenditService.parseInvoiceWebhook(payload);
    String externalId = webhook.getExternalId();
    String status = webhook.getStatus();

    log.info("Received invoice webhook - status: {}, external_id: {}", status, externalId);

    if (externalId != null && externalId.startsWith("phase_payment_")) {
      switch (status) {
        case "PAID":
          paymentService.handleInvoicePaid(webhook);
          break;
        case "EXPIRED":
          paymentService.handleInvoiceExpired(webhook);
          break;
        default:
          log.info("Unhandled invoice status for phase payment: {}", status);
      }
    } else {
      switch (status) {
        case "PAID":
          tokenPurchaseService.handleInvoicePaid(webhook);
          break;
        case "EXPIRED":
          tokenPurchaseService.handleInvoiceExpired(webhook);
          break;
        default:
          log.info("Unhandled invoice status for token purchase: {}", status);
      }
    }

    return ResponseEntity.ok().build();
  }
}
