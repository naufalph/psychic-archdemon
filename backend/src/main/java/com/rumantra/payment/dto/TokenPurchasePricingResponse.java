package com.rumantra.payment.dto;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenPurchasePricingResponse {
  private String currentTier;
  private BigDecimal pricePerToken;
  private String currency;
  private Integer minQuantity;
  private Integer maxQuantity;
  private Map<String, BigDecimal> tierPricing;
}
