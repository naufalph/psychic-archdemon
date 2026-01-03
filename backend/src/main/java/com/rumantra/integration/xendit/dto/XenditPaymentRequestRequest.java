package com.rumantra.integration.xendit.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XenditPaymentRequestRequest {
  @JsonProperty("reference_id")
  private String referenceId;

  @JsonProperty("type")
  @Builder.Default
  private String type = "PAY";

  @JsonProperty("country")
  @Builder.Default
  private String country = "ID";

  @JsonProperty("currency")
  @Builder.Default
  private String currency = "IDR";

  @JsonProperty("request_amount")
  private BigDecimal requestAmount;

  @JsonProperty("capture_method")
  @Builder.Default
  private String captureMethod = "AUTOMATIC";

  @JsonProperty("description")
  private String description;

  @JsonProperty("metadata")
  private Map<String, String> metadata;
}
