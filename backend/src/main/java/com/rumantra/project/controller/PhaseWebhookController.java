package com.rumantra.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.integration.xendit.XenditService;
import com.rumantra.integration.xendit.dto.XenditPayoutCallback;
import com.rumantra.project.service.PhasePaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Phase Payment")
@RestController
@Slf4j
public class PhaseWebhookController {

  @Autowired private PhasePaymentService phasePaymentService;
  @Autowired private XenditService xenditService;

  @Operation(summary = "Xendit payout webhook (public)")
  @PostMapping("/rmtr/phases/webhook/payout")
  public ResponseEntity<Void> handlePayoutWebhook(
      @RequestHeader("X-CALLBACK-TOKEN") String callbackToken, @RequestBody String payload) {

    if (!xenditService.verifyWebhookToken(callbackToken)) {
      log.warn("Invalid callback token for payout webhook");
      return ResponseEntity.status(403).build();
    }

    XenditPayoutCallback callback = xenditService.parsePayoutCallback(payload);
    log.info(
        "Received payout webhook: event={}, payoutId={}", callback.getEvent(), callback.getId());

    phasePaymentService.handlePayoutCallback(callback);

    return ResponseEntity.ok().build();
  }
}
