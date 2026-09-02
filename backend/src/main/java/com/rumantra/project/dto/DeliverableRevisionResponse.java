package com.rumantra.project.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One instruction the client left on a deliverable, in the round it was asked for. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliverableRevisionResponse {

  private Integer round;
  private String notes;
  private LocalDateTime requestedAt;
}
