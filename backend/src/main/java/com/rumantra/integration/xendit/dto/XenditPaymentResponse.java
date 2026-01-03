package com.rumantra.integration.xendit.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class XenditPaymentResponse {
  @JsonProperty("id")
  private String id;

  @JsonProperty("reference_id")
  private String referenceId;

  @JsonProperty("type")
  private String type;

  @JsonProperty("country")
  private String country;

  @JsonProperty("currency")
  private String currency;

  @JsonProperty("amount")
  private BigDecimal amount;

  @JsonProperty("status")
  private String status;

  @JsonProperty("description")
  private String description;

  @JsonProperty("metadata")
  private Map<String, String> metadata;

  @JsonProperty("actions")
  private List<XenditActionUrl> actions;

  @JsonProperty("created")
  private String created;

  @JsonProperty("updated")
  private String updated;
}
