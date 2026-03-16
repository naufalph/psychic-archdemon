package com.rumantra.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.chat.domain.Conversation;
import com.rumantra.chat.dto.ConversationResponse;
import com.rumantra.chat.event.SupportRequestedEvent;
import com.rumantra.chat.repository.ConversationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupportConversationService {

  private final ConversationRepository conversationRepository;
  private final ConversationService conversationService;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public ConversationResponse getOrCreate(Long projectId, Long bidId) {
    Conversation conv =
        conversationRepository
            .findProjectConversationByBidId(bidId)
            .orElseThrow(() -> new RuntimeException("Project conversation not found"));

    if (!Boolean.TRUE.equals(conv.getItSupportRequested())) {
      conv.setItSupportRequested(true);
      conv.setItSupportRequestedAt(LocalDateTime.now());
      conversationRepository.save(conv);
      eventPublisher.publishEvent(new SupportRequestedEvent(this, conv));
    }

    return conversationService.mapToResponse(conv);
  }

  public List<ConversationResponse> getAllForSuperuser() {
    return conversationRepository
        .findProjectConversationsWithSupportRequestedOrderByLastMessageAtDesc()
        .stream()
        .map(conversationService::mapToResponse)
        .collect(Collectors.toList());
  }
}
