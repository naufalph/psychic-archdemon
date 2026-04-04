package com.rumantra.integration.xendit.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class XenditInvoiceWebhook {
  @JsonProperty("id")
  private String id;

  @JsonProperty("external_id")
  private String externalId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("status")
  private String status; // PAID, EXPIRED

  @JsonProperty("paid_amount")
  private BigDecimal paidAmount;

  @JsonProperty("paid_at")
  private String paidAt;

  @JsonProperty("payment_channel")
  private String paymentChannel; // BCA, DANA, etc.

  @JsonProperty("payment_method")
  private String paymentMethod; // BANK_TRANSFER, EWALLET, etc.

  @JsonProperty("currency")
  private String currency;

  @JsonProperty("description")
  private String description;
}
