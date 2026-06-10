package com.rumantra.integration.xendit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.integration.xendit.XenditService;
import com.rumantra.integration.xendit.dto.XenditInvoiceWebhook;
import com.rumantra.integration.xendit.dto.XenditPayoutCallback;
import com.rumantra.payment.service.PaymentService;
import com.rumantra.payment.service.TokenPurchaseService;
import com.rumantra.project.service.PhasePaymentService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rmtr/xendit")
@Slf4j
public class XenditWebhookController {

  @Autowired private XenditService xenditService;
  @Autowired private PhasePaymentService phasePaymentService;
  @Autowired private PaymentService paymentService;
  @Autowired private TokenPurchaseService tokenPurchaseService;

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

    if (externalId != null && externalId.startsWith("proj_phase_")) {
      switch (status) {
        case "PAID":
          phasePaymentService.handlePaymentWebhook(webhook);
          break;
        case "EXPIRED":
          phasePaymentService.handleInvoiceExpired(webhook);
          break;
        default:
          log.info("Unhandled invoice status for proj_phase: {}", status);
      }
    } else if (externalId != null && externalId.startsWith("phase_payment_")) {
      switch (status) {
        case "PAID":
          paymentService.handleInvoicePaid(webhook);
          break;
        case "EXPIRED":
          paymentService.handleInvoiceExpired(webhook);
          break;
        default:
          log.info("Unhandled invoice status for phase_payment: {}", status);
      }
    } else if (externalId != null && externalId.startsWith("token_purchase_")) {
      switch (status) {
        case "PAID":
          tokenPurchaseService.handleInvoicePaid(webhook);
          break;
        case "EXPIRED":
          tokenPurchaseService.handleInvoiceExpired(webhook);
          break;
        default:
          log.info("Unhandled invoice status for token_purchase: {}", status);
      }
    } else {
      log.warn("Received invoice webhook with unknown external_id prefix: {}", externalId);
    }

    return ResponseEntity.ok().build();
  }

  @PostMapping("/webhook/payout")
  public ResponseEntity<Void> handlePayoutWebhook(
      @RequestHeader("X-CALLBACK-TOKEN") String callbackToken, @RequestBody String payload) {

    if (!xenditService.verifyWebhookToken(callbackToken)) {
      log.warn("Invalid webhook token received for payout");
      return ResponseEntity.status(403).build();
    }

    XenditPayoutCallback callback = xenditService.parsePayoutCallback(payload);
    log.info(
        "Received payout webhook: event={}, payoutId={}", callback.getEvent(), callback.getId());

    phasePaymentService.handlePayoutCallback(callback);

    return ResponseEntity.ok().build();
  }
}
