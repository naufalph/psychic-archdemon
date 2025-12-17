package com.rumantra.integration.xendit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class XenditWebhookEvent {
  @JsonProperty("id")
  private String id;

  @JsonProperty("reference_id")
  private String referenceId;

  @JsonProperty("customer_id")
  private String customerId;

  @JsonProperty("recurring_action")
  private String recurringAction;

  @JsonProperty("status")
  private String status;

  @JsonProperty("currency")
  private String currency;

  @JsonProperty("amount")
  private String amount;

  @JsonProperty("recurring_cycle_id")
  private String recurringCycleId;

  @JsonProperty("created")
  private String created;

  @JsonProperty("updated")
  private String updated;
}
