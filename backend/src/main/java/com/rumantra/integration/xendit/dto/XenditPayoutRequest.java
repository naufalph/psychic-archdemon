package com.rumantra.integration.xendit.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XenditPayoutRequest {

  @JsonProperty("reference_id")
  private String referenceId;

  @JsonProperty("channel_code")
  private String channelCode;

  @JsonProperty("channel_properties")
  private ChannelProperties channelProperties;

  @JsonProperty("amount")
  private BigDecimal amount;

  @JsonProperty("currency")
  @Builder.Default
  private String currency = "IDR";

  @JsonProperty("description")
  private String description;

  @Data
  @Builder
  public static class ChannelProperties {
    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_holder_name")
    private String accountHolderName;
  }
}
