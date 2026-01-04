package com.rumantra.chat.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.rumantra.chat.event.BidAcceptedEvent;
import com.rumantra.chat.service.ConversationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ChatEventListener {

  @Autowired private ConversationService conversationService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void handleBidAccepted(BidAcceptedEvent event) {
    try {
      conversationService.createConversation(
          event.getBidId(), event.getProjectId(), event.getArchitectId(), event.getClientId());

      log.info(
          "Created conversation for accepted bid: bidId={}, projectId={}",
          event.getBidId(),
          event.getProjectId());
    } catch (Exception e) {
      log.error(
          "Failed to create conversation for bid {}: {}", event.getBidId(), e.getMessage(), e);
    }
  }
}
