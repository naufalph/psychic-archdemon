package com.rumantra.integration.xendit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class XenditActions {
  @JsonProperty("desktop_web_checkout_url")
  private String desktopWebCheckoutUrl;

  @JsonProperty("mobile_web_checkout_url")
  private String mobileWebCheckoutUrl;

  @JsonProperty("mobile_deeplink_checkout_url")
  private String mobileDeeplinkCheckoutUrl;

  @JsonProperty("qr_checkout_string")
  private String qrCheckoutString;
}
