package com.rumantra.project.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.rumantra.integration.xendit.XenditService;
import com.rumantra.integration.xendit.dto.XenditPayoutCallback;
import com.rumantra.project.service.PhasePaymentService;

@ExtendWith(MockitoExtension.class)
class PhaseWebhookControllerTest {

  @InjectMocks private PhaseWebhookController controller;

  @Mock private PhasePaymentService phasePaymentService;
  @Mock private XenditService xenditService;

  private static final String VALID_TOKEN = "valid-webhook-token";
  private static final String PAYOUT_PAYLOAD =
      "{\"event\":\"payout.succeeded\",\"id\":\"payout_123\",\"reference_id\":\"ref_123\"}";

  @Test
  void payout_webhook_returns403_whenTokenMissing() {
    when(xenditService.verifyWebhookToken("bad-token")).thenReturn(false);

    ResponseEntity<Void> response = controller.handlePayoutWebhook("bad-token", PAYOUT_PAYLOAD);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(phasePaymentService, never()).handlePayoutCallback(any());
  }

  @Test
  void payout_webhook_returns200_andProcesses_whenTokenValid() {
    when(xenditService.verifyWebhookToken(VALID_TOKEN)).thenReturn(true);
    XenditPayoutCallback callback = new XenditPayoutCallback();
    callback.setEvent("payout.succeeded");
    when(xenditService.parsePayoutCallback(eq(PAYOUT_PAYLOAD))).thenReturn(callback);
    doNothing().when(phasePaymentService).handlePayoutCallback(callback);

    ResponseEntity<Void> response = controller.handlePayoutWebhook(VALID_TOKEN, PAYOUT_PAYLOAD);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(phasePaymentService).handlePayoutCallback(callback);
  }
}
