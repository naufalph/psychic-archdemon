package com.rumantra.integration.xendit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumantra.integration.xendit.dto.XenditCreatePlanRequest;
import com.rumantra.integration.xendit.dto.XenditCreatePlanResponse;
import com.rumantra.integration.xendit.dto.XenditWebhookEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class XenditService {

  private final XenditClient xenditClient;

  @Value("${xendit.webhook-token}")
  private String webhookToken;

  public XenditService(XenditClient xenditClient) {
    this.xenditClient = xenditClient;
  }

  public XenditCreatePlanResponse createRecurringPlan(XenditCreatePlanRequest request) {
    log.info("Creating Xendit recurring plan for {}", request.getReferenceId());
    return xenditClient.post("/recurring_payments", request, XenditCreatePlanResponse.class);
  }

  public void pauseRecurringPlan(String planId) {
    log.info("Pausing recurring plan: {}", planId);
    xenditClient.post("/recurring_payments/" + planId + "/pause", null, Void.class);
  }

  public void stopRecurringPlan(String planId) {
    log.info("Stopping recurring plan: {}", planId);
    xenditClient.post("/recurring_payments/" + planId + "/stop", null, Void.class);
  }

  public boolean verifyWebhookToken(String receivedToken) {
    if (receivedToken == null || webhookToken == null) {
      log.warn("Webhook token verification failed: null token");
      return false;
    }
    return webhookToken.equals(receivedToken);
  }

  public XenditWebhookEvent parseWebhook(String payload) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(payload, XenditWebhookEvent.class);
    } catch (Exception e) {
      log.error("Failed to parse Xendit webhook", e);
      throw new XenditException("Invalid webhook payload", e);
    }
  }
}
