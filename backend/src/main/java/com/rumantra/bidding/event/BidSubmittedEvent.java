package com.rumantra.bidding.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class BidSubmittedEvent extends ApplicationEvent {

  private final Long bidId;
  private final Long projectId;
  private final Long architectId;
  private final Long clientId;
  private final Long clientUserId;
  private final String projectTitle;
  private final String architectName;

  public BidSubmittedEvent(
      Object source,
      Long bidId,
      Long projectId,
      Long architectId,
      Long clientId,
      Long clientUserId,
      String projectTitle,
      String architectName) {
    super(source);
    this.bidId = bidId;
    this.projectId = projectId;
    this.architectId = architectId;
    this.clientId = clientId;
    this.clientUserId = clientUserId;
    this.projectTitle = projectTitle;
    this.architectName = architectName;
  }
}
