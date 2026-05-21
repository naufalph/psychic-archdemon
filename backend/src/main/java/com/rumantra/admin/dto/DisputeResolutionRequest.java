package com.rumantra.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DisputeResolutionRequest {

  @NotNull private Decision decision;

  private String note;

  public enum Decision {
    APPROVE,
    REJECT
  }
}
