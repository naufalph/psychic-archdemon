package com.rumantra.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminNegotiationResolutionRequest {

  @NotNull private Decision decision;

  private String note;

  public enum Decision {
    CLIENT_ABANDONED,
    ARCHITECT_ABANDONED
  }
}
