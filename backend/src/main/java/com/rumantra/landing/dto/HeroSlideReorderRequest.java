package com.rumantra.landing.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HeroSlideReorderRequest {

  @NotEmpty(message = "Ordered slide IDs are required")
  private List<Long> orderedIds;
}
