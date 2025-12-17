package com.rumantra.integration.xendit.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XenditCreatePlanRequest {
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

  @JsonProperty("schedule")
  private XenditSchedule schedule;

  @JsonProperty("failed_cycle_action")
  private String failedCycleAction;

  @JsonProperty("success_return_url")
  private String successReturnUrl;

  @JsonProperty("failure_return_url")
  private String failureReturnUrl;
}
