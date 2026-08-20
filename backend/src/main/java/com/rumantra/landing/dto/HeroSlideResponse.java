package com.rumantra.landing.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeroSlideResponse {

  private Long id;
  private String imageUrl;
  private String imageLargeUrl;
  private String architectName;
  private String avatarInitial;
  private boolean verified;
  private BigDecimal rating;
  private String reviewQuote;
  private String reviewerName;
  private int displayOrder;
  private boolean active;
}
