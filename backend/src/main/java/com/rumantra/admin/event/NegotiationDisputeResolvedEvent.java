package com.rumantra.admin.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class NegotiationDisputeResolvedEvent extends ApplicationEvent {

  private final Long projectId;
  private final String projectTitle;
  private final Long clientUserId;
  private final String clientEmail;
  private final Long architectUserId;
  private final String architectEmail;
  private final String decision;

  public NegotiationDisputeResolvedEvent(
      Object source,
      Long projectId,
      String projectTitle,
      Long clientUserId,
      String clientEmail,
      Long architectUserId,
      String architectEmail,
      String decision) {
    super(source);
    this.projectId = projectId;
    this.projectTitle = projectTitle;
    this.clientUserId = clientUserId;
    this.clientEmail = clientEmail;
    this.architectUserId = architectUserId;
    this.architectEmail = architectEmail;
    this.decision = decision;
  }
}
