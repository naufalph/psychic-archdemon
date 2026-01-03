package com.rumantra.payment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class TokenPurchaseInitiateRequest {
  @Min(value = 1, message = "Minimum quantity is 1 token")
  @Max(value = 50, message = "Maximum quantity is 50 tokens")
  private Integer quantity;
}
