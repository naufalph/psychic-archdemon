package com.rumantra.integration.xendit.controller;

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
import com.rumantra.integration.xendit.dto.XenditInvoiceWebhook;
import com.rumantra.integration.xendit.dto.XenditPayoutCallback;
import com.rumantra.payment.service.PaymentService;
import com.rumantra.payment.service.TokenPurchaseService;
import com.rumantra.project.service.PhasePaymentService;

@ExtendWith(MockitoExtension.class)
class XenditWebhookControllerTest {

  @InjectMocks private XenditWebhookController controller;

  @Mock private XenditService xenditService;
  @Mock private PhasePaymentService phasePaymentService;
  @Mock private PaymentService paymentService;
  @Mock private TokenPurchaseService tokenPurchaseService;

  private static final String VALID_TOKEN = "valid-webhook-token";
  private static final String PAYOUT_PAYLOAD =
      "{\"event\":\"payout.succeeded\",\"id\":\"payout_123\",\"reference_id\":\"ref_123\"}";
  private static final String INVOICE_PAYLOAD = "{\"status\":\"PAID\",\"external_id\":\"%s\"}";

  @Test
  void payout_webhook_returns403_whenTokenInvalid() {
    when(xenditService.verifyWebhookToken("bad-token")).thenReturn(false);

    ResponseEntity<Void> response = controller.handlePayoutWebhook("bad-token", PAYOUT_PAYLOAD);

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verify(phasePaymentService, never()).handlePayoutCallback(any());
  }

  @Test
  void payout_webhook_returns200_andDelegates_whenTokenValid() {
    when(xenditService.verifyWebhookToken(VALID_TOKEN)).thenReturn(true);
    XenditPayoutCallback callback = new XenditPayoutCallback();
    callback.setEvent("payout.succeeded");
    when(xenditService.parsePayoutCallback(eq(PAYOUT_PAYLOAD))).thenReturn(callback);
    doNothing().when(phasePaymentService).handlePayoutCallback(callback);

    ResponseEntity<Void> response = controller.handlePayoutWebhook(VALID_TOKEN, PAYOUT_PAYLOAD);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(phasePaymentService).handlePayoutCallback(callback);
  }

  @Test
  void invoice_webhook_routes_projPhase_toPaid() {
    String payload = String.format(INVOICE_PAYLOAD, "proj_phase_1_123");
    when(xenditService.verifyWebhookToken(VALID_TOKEN)).thenReturn(true);
    XenditInvoiceWebhook webhook = new XenditInvoiceWebhook();
    webhook.setExternalId("proj_phase_1_123");
    webhook.setStatus("PAID");
    when(xenditService.parseInvoiceWebhook(payload)).thenReturn(webhook);

    controller.handleInvoiceWebhook(VALID_TOKEN, payload);

    verify(phasePaymentService).handlePaymentWebhook(webhook);
    verifyNoInteractions(paymentService, tokenPurchaseService);
  }

  @Test
  void invoice_webhook_routes_phasePayment_toPaid() {
    String payload = String.format(INVOICE_PAYLOAD, "phase_payment_proj_1_phase_5_123");
    when(xenditService.verifyWebhookToken(VALID_TOKEN)).thenReturn(true);
    XenditInvoiceWebhook webhook = new XenditInvoiceWebhook();
    webhook.setExternalId("phase_payment_proj_1_phase_5_123");
    webhook.setStatus("PAID");
    when(xenditService.parseInvoiceWebhook(payload)).thenReturn(webhook);

    controller.handleInvoiceWebhook(VALID_TOKEN, payload);

    verify(paymentService).handleInvoicePaid(webhook);
    verifyNoInteractions(phasePaymentService, tokenPurchaseService);
  }

  @Test
  void invoice_webhook_routes_tokenPurchase_toPaid() {
    String payload = String.format(INVOICE_PAYLOAD, "token_purchase_arch_42_123");
    when(xenditService.verifyWebhookToken(VALID_TOKEN)).thenReturn(true);
    XenditInvoiceWebhook webhook = new XenditInvoiceWebhook();
    webhook.setExternalId("token_purchase_arch_42_123");
    webhook.setStatus("PAID");
    when(xenditService.parseInvoiceWebhook(payload)).thenReturn(webhook);

    controller.handleInvoiceWebhook(VALID_TOKEN, payload);

    verify(tokenPurchaseService).handleInvoicePaid(webhook);
    verifyNoInteractions(phasePaymentService, paymentService);
  }

  @Test
  void invoice_webhook_routes_projPhase_toExpired() {
    String payload = "{\"status\":\"EXPIRED\",\"external_id\":\"proj_phase_1_123\"}";
    when(xenditService.verifyWebhookToken(VALID_TOKEN)).thenReturn(true);
    XenditInvoiceWebhook webhook = new XenditInvoiceWebhook();
    webhook.setExternalId("proj_phase_1_123");
    webhook.setStatus("EXPIRED");
    when(xenditService.parseInvoiceWebhook(payload)).thenReturn(webhook);

    controller.handleInvoiceWebhook(VALID_TOKEN, payload);

    verify(phasePaymentService).handleInvoiceExpired(webhook);
    verifyNoInteractions(paymentService, tokenPurchaseService);
  }

  @Test
  void invoice_webhook_returns403_whenTokenInvalid() {
    when(xenditService.verifyWebhookToken("bad-token")).thenReturn(false);

    ResponseEntity<Void> response =
        controller.handleInvoiceWebhook("bad-token", "{\"status\":\"PAID\"}");

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    verifyNoInteractions(phasePaymentService, paymentService, tokenPurchaseService);
  }
}
