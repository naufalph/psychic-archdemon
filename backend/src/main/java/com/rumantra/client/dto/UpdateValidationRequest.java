package com.rumantra.client.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateValidationRequest {

  @NotNull(message = "isValid field is required")
  private Boolean isValid;

  private String validationNotes;
}
