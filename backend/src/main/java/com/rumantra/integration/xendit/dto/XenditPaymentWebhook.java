package com.rumantra.integration.xendit.dto;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class XenditPaymentWebhook {
  @JsonProperty("event")
  private String event;

  @JsonProperty("business_id")
  private String businessId;

  @JsonProperty("created")
  private String created;

  @JsonProperty("data")
  private PaymentData data;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PaymentData {
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

    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;

    @JsonProperty("created")
    private String created;

    @JsonProperty("updated")
    private String updated;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PaymentMethod {
    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("card")
    private CardDetails card;

    @JsonProperty("ewallet")
    private EwalletDetails ewallet;

    @JsonProperty("direct_debit")
    private DirectDebitDetails directDebit;

    @JsonProperty("over_the_counter")
    private OverTheCounterDetails overTheCounter;

    @JsonProperty("virtual_account")
    private VirtualAccountDetails virtualAccount;

    @JsonProperty("qr_code")
    private QrCodeDetails qrCode;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CardDetails {
    @JsonProperty("channel_code")
    private String channelCode;

    @JsonProperty("masked_card_number")
    private String maskedCardNumber;

    @JsonProperty("card_brand")
    private String cardBrand;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class EwalletDetails {
    @JsonProperty("channel_code")
    private String channelCode;

    @JsonProperty("account_details")
    private String accountDetails;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DirectDebitDetails {
    @JsonProperty("channel_code")
    private String channelCode;

    @JsonProperty("masked_account_number")
    private String maskedAccountNumber;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class OverTheCounterDetails {
    @JsonProperty("channel_code")
    private String channelCode;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class VirtualAccountDetails {
    @JsonProperty("channel_code")
    private String channelCode;

    @JsonProperty("account_number")
    private String accountNumber;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class QrCodeDetails {
    @JsonProperty("channel_code")
    private String channelCode;
  }
}
