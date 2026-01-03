package com.rumantra.integration.xendit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class XenditActionUrl {
  @JsonProperty("action")
  private String action;

  @JsonProperty("url")
  private String url;

  @JsonProperty("url_type")
  private String urlType;
}
