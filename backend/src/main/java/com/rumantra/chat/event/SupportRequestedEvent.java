package com.rumantra.chat.event;

import org.springframework.context.ApplicationEvent;

import com.rumantra.chat.domain.Conversation;

import lombok.Getter;

@Getter
public class SupportRequestedEvent extends ApplicationEvent {

  private final Long conversationId;
  private final Long projectId;
  private final Long bidId;
  private final Long requesterUserId;

  public SupportRequestedEvent(Object source, Conversation conversation) {
    super(source);
    this.conversationId = conversation.getId();
    this.projectId = conversation.getProjectId();
    this.bidId = conversation.getBidId();
    this.requesterUserId = conversation.getRequesterUserId();
  }
}
