package com.rumantra.integration.xendit.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class XenditPayoutCallback {

  @JsonProperty("event")
  private String event;

  @JsonProperty("id")
  private String id;

  @JsonProperty("reference_id")
  private String referenceId;

  @JsonProperty("status")
  private String status;

  @JsonProperty("channel_code")
  private String channelCode;

  @JsonProperty("failure_code")
  private String failureCode;

  @JsonProperty("amount")
  private BigDecimal amount;

  @JsonProperty("updated")
  private String updated;
}
