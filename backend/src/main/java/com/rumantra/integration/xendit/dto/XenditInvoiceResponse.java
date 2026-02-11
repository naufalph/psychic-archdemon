package com.rumantra.integration.xendit.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class XenditInvoiceResponse {
  @JsonProperty("id")
  private String id;

  @JsonProperty("external_id")
  private String externalId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("status")
  private String status; // PENDING, PAID, EXPIRED

  @JsonProperty("merchant_name")
  private String merchantName;

  @JsonProperty("amount")
  private BigDecimal amount;

  @JsonProperty("currency")
  private String currency;

  @JsonProperty("description")
  private String description;

  @JsonProperty("invoice_url")
  private String invoiceUrl;

  @JsonProperty("expiry_date")
  private String expiryDate;

  @JsonProperty("created")
  private String created;

  @JsonProperty("updated")
  private String updated;
}
