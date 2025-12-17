package com.rumantra.integration.xendit.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XenditSchedule {
  @JsonProperty("reference_id")
  private String referenceId;

  @JsonProperty("interval")
  private String interval;

  @JsonProperty("interval_count")
  private Integer intervalCount;

  @JsonProperty("anchor_date")
  private LocalDateTime anchorDate;

  @JsonProperty("retry_interval")
  private String retryInterval;

  @JsonProperty("retry_interval_count")
  private Integer retryIntervalCount;

  @JsonProperty("total_retry")
  private Integer totalRetry;

  @JsonProperty("failed_attempt_notifications")
  private List<Integer> failedAttemptNotifications;
}
