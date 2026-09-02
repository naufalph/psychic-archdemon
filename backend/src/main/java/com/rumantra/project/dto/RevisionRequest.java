package com.rumantra.project.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A revision request covers one or more deliverables of a phase and costs a single revision round
 * between them — the count is pooled at the phase, the instructions are per deliverable.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevisionRequest {

  private List<Item> items;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Item {
    private Integer index;
    private String notes;
  }
}
