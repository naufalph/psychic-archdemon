package com.rumantra.integration.xendit.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class XenditPayoutResponse {

  @JsonProperty("id")
  private String id;

  @JsonProperty("status")
  private String status;

  @JsonProperty("reference_id")
  private String referenceId;

  @JsonProperty("channel_code")
  private String channelCode;

  @JsonProperty("currency")
  private String currency;

  @JsonProperty("amount")
  private BigDecimal amount;

  @JsonProperty("created")
  private String created;

  @JsonProperty("updated")
  private String updated;
}
