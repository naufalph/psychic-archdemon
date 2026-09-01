package com.rumantra.project.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One deliverable named in the accepted bid, with the files tagged to it.
 *
 * <p>{@code status} is derived, not stored: APPROVED comes from the approval projection, LOCKED
 * from the phase not having started, MISSING from having no files yet.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliverableItemResponse {

  private Integer index;
  private String name;
  private String status;
  private List<DeliverableResponse> files;
}
