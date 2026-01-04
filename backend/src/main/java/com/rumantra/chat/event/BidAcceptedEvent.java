package com.rumantra.chat.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class BidAcceptedEvent extends ApplicationEvent {

  private final Long bidId;
  private final Long projectId;
  private final Long architectId;
  private final Long clientId;
  private final Long acceptedByUserId;

  public BidAcceptedEvent(
      Object source,
      Long bidId,
      Long projectId,
      Long architectId,
      Long clientId,
      Long acceptedByUserId) {
    super(source);
    this.bidId = bidId;
    this.projectId = projectId;
    this.architectId = architectId;
    this.clientId = clientId;
    this.acceptedByUserId = acceptedByUserId;
  }
}
