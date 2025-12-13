package com.rumantra.bidding.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkPortfoliosRequest {

  @NotEmpty(message = "Portfolio IDs are required")
  @Size(max = 3, message = "Maximum 3 portfolios allowed")
  private List<Long> portfolioIds;
}
