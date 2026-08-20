package com.rumantra.landing.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeroSlideRequest {

  @NotBlank(message = "Architect name is required")
  @Size(max = 120, message = "Architect name must not exceed 120 characters")
  private String architectName;

  @Size(max = 2, message = "Avatar initial must not exceed 2 characters")
  private String avatarInitial;

  private Boolean verified;

  @DecimalMin(value = "0.0", message = "Rating must be at least 0")
  @DecimalMax(value = "5.0", message = "Rating must not exceed 5")
  private BigDecimal rating;

  private String reviewQuote;

  @Size(max = 120, message = "Reviewer name must not exceed 120 characters")
  private String reviewerName;

  private Boolean active;

  /** Set on update to drop the existing image without supplying a replacement. */
  private Boolean removeImage;
}
