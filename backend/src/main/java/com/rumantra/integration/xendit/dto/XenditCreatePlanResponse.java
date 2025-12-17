package com.rumantra.integration.xendit.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class XenditCreatePlanResponse {
  @JsonProperty("id")
  private String id;

  @JsonProperty("reference_id")
  private String referenceId;

  @JsonProperty("customer_id")
  private String customerId;

  @JsonProperty("recurring_action")
  private String recurringAction;

  @JsonProperty("currency")
  private String currency;

  @JsonProperty("amount")
  private BigDecimal amount;

  @JsonProperty("status")
  private String status;

  @JsonProperty("schedule")
  private XenditSchedule schedule;

  @JsonProperty("actions")
  private XenditActions actions;

  @JsonProperty("created")
  private String created;

  @JsonProperty("updated")
  private String updated;
}
