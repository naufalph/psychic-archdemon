package com.rumantra.integration.xendit.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XenditInvoiceRequest {
  @JsonProperty("external_id")
  private String externalId;

  @JsonProperty("amount")
  private BigDecimal amount;

  @JsonProperty("description")
  private String description;

  @JsonProperty("currency")
  @Builder.Default
  private String currency = "IDR";

  @JsonProperty("invoice_duration")
  @Builder.Default
  private Integer invoiceDuration = 86400; // 24 hours

  @JsonProperty("success_redirect_url")
  private String successRedirectUrl;

  @JsonProperty("failure_redirect_url")
  private String failureRedirectUrl;

  @JsonProperty("customer")
  private Customer customer;

  @JsonProperty("customer_notification_preference")
  private CustomerNotificationPreference customerNotificationPreference;

  @JsonProperty("items")
  private List<InvoiceItem> items;

  @Data
  @Builder
  public static class Customer {
    @JsonProperty("given_names")
    private String givenNames;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("email")
    private String email;
  }

  @Data
  @Builder
  public static class CustomerNotificationPreference {
    @JsonProperty("invoice_created")
    @Builder.Default
    private List<String> invoiceCreated = new ArrayList<>();

    @JsonProperty("invoice_reminder")
    @Builder.Default
    private List<String> invoiceReminder = new ArrayList<>();

    @JsonProperty("invoice_paid")
    @Builder.Default
    private List<String> invoicePaid = Arrays.asList("email");
  }

  @Data
  @Builder
  public static class InvoiceItem {
    @JsonProperty("name")
    private String name;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("category")
    @Builder.Default
    private String category = "Digital Service";
  }
}
