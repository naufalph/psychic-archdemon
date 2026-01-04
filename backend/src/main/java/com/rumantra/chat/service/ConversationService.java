package com.rumantra.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.chat.domain.Conversation;
import com.rumantra.chat.domain.ConversationStatus;
import com.rumantra.chat.domain.Message;
import com.rumantra.chat.dto.ConversationResponse;
import com.rumantra.chat.dto.MessageResponse;
import com.rumantra.chat.repository.ConversationRepository;
import com.rumantra.chat.repository.MessageRepository;
import com.rumantra.client.domain.Client;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;

@Service
public class ConversationService {

  @Autowired private ConversationRepository conversationRepository;

  @Autowired private MessageRepository messageRepository;

  @Autowired private ArchitectRepository architectRepository;

  @Autowired private ClientRepository clientRepository;

  @Transactional
  public void createConversation(Long bidId, Long projectId, Long architectId, Long clientId) {
    if (conversationRepository.findByBidId(bidId).isPresent()) {
      conversationRepository.findByBidId(bidId).get();
      return;
    }

    Conversation conversation =
        Conversation.builder()
            .bidId(bidId)
            .projectId(projectId)
            .architectId(architectId)
            .clientId(clientId)
            .status(ConversationStatus.ACTIVE)
            .build();

    conversationRepository.save(conversation);
  }

  public List<ConversationResponse> getMyConversations() {
    Long userId = SecurityUtils.getCurrentUserId();

    Architect architect = architectRepository.findByUserId(userId).orElse(null);
    Client client = clientRepository.findByUserId(userId).orElse(null);

    List<Conversation> conversations;
    if (architect != null && client != null) {
      conversations =
          conversationRepository.findByUserIdOrderByLastMessageAtDesc(
              architect.getId(), client.getId());
    } else if (architect != null) {
      conversations =
          conversationRepository.findByArchitectIdOrderByLastMessageAtDesc(architect.getId());
    } else if (client != null) {
      conversations = conversationRepository.findByClientIdOrderByLastMessageAtDesc(client.getId());
    } else {
      throw new RuntimeException("User has no architect or client role");
    }

    return conversations.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  public ConversationResponse getConversationById(Long conversationId) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));

    verifyAccess(conversation);

    return mapToResponse(conversation);
  }

  @Transactional
  public void updateLastMessageTime(Long conversationId) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));

    conversation.setLastMessageAt(LocalDateTime.now());
    conversationRepository.save(conversation);
  }

  @Transactional
  public void archiveConversation(Long conversationId) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));

    verifyAccess(conversation);

    conversation.setStatus(ConversationStatus.ARCHIVED);
    conversationRepository.save(conversation);
  }

  public void verifyAccess(Conversation conversation) {
    Long userId = SecurityUtils.getCurrentUserId();

    Architect architect = architectRepository.findByUserId(userId).orElse(null);
    Client client = clientRepository.findByUserId(userId).orElse(null);

    boolean hasAccess =
        architect != null && conversation.getArchitectId().equals(architect.getId());
    if (client != null && conversation.getClientId().equals(client.getId())) {
      hasAccess = true;
    }

    if (!hasAccess) {
      throw new RuntimeException("Unauthorized access to conversation");
    }
  }

  private ConversationResponse mapToResponse(Conversation conversation) {
    Long userId = SecurityUtils.getCurrentUserId();

    Architect architect =
        architectRepository
            .findById(conversation.getArchitectId())
            .orElseThrow(() -> new BusinessException(ExceptionConstants.ARCHITECT_NOT_FOUND));

    Client client =
        clientRepository
            .findById(conversation.getClientId())
            .orElseThrow(() -> new RuntimeException("Client not found"));

    String architectName =
        architect.getUser().getFirstName() + " " + architect.getUser().getLastName();
    String clientName = client.getUser().getFirstName() + " " + client.getUser().getLastName();

    Integer unreadCount = messageRepository.countUnreadMessages(conversation.getId(), userId);

    Message lastMessage =
        messageRepository
            .findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId())
            .orElse(null);

    MessageResponse lastMessageResponse = null;
    if (lastMessage != null) {
      String senderName;
      if (lastMessage.getSenderUserId().equals(architect.getUser().getId())) {
        senderName = architectName;
      } else {
        senderName = clientName;
      }

      lastMessageResponse =
          MessageResponse.builder()
              .id(lastMessage.getId())
              .conversationId(lastMessage.getConversationId())
              .senderUserId(lastMessage.getSenderUserId())
              .senderName(senderName)
              .senderType(lastMessage.getSenderType())
              .content(lastMessage.getContent())
              .messageType(lastMessage.getMessageType())
              .isRead(lastMessage.getIsRead())
              .readAt(lastMessage.getReadAt())
              .createdAt(lastMessage.getCreatedAt())
              .build();
    }

    return ConversationResponse.builder()
        .id(conversation.getId())
        .projectId(conversation.getProjectId())
        .bidId(conversation.getBidId())
        .architectId(conversation.getArchitectId())
        .architectName(architectName)
        .clientId(conversation.getClientId())
        .clientName(clientName)
        .status(conversation.getStatus())
        .unreadCount(unreadCount)
        .lastMessage(lastMessageResponse)
        .lastMessageAt(conversation.getLastMessageAt())
        .createdAt(conversation.getCreatedAt())
        .build();
  }
}
