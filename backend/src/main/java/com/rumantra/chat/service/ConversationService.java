package com.rumantra.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
import com.rumantra.chat.domain.SenderType;
import com.rumantra.chat.dto.ConversationResponse;
import com.rumantra.chat.dto.MessageResponse;
import com.rumantra.chat.repository.ConversationRepository;
import com.rumantra.chat.repository.MessageRepository;
import com.rumantra.client.domain.Client;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

@Service
public class ConversationService {

  @Autowired private ConversationRepository conversationRepository;

  @Autowired private MessageRepository messageRepository;

  @Autowired private ArchitectRepository architectRepository;

  @Autowired private ClientRepository clientRepository;

  @Autowired private UserRepository userRepository;

  @Transactional
  public void createConversation(Long bidId, Long projectId, Long architectId, Long clientId) {
    if (conversationRepository.findProjectConversationByBidId(bidId).isPresent()) {
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

    if (SecurityUtils.hasRole("SUPERUSER")) {
      return conversationRepository
          .findProjectConversationsWithSupportRequestedOrderByLastMessageAtDesc()
          .stream()
          .map(this::mapToResponse)
          .collect(Collectors.toList());
    }

    Architect architect = architectRepository.findByUserId(userId).orElse(null);
    Client client = clientRepository.findByUserId(userId).orElse(null);

    List<Conversation> conversations = new ArrayList<>();

    if (architect != null && client != null) {
      conversations.addAll(
          conversationRepository.findProjectConversationsByUserIdOrderByLastMessageAtDesc(
              architect.getId(), client.getId()));
    } else if (architect != null) {
      conversations.addAll(
          conversationRepository.findProjectConversationsByArchitectId(architect.getId()));
    } else if (client != null) {
      conversations.addAll(
          conversationRepository.findProjectConversationsByClientId(client.getId()));
    }

    conversations.sort(
        Comparator.comparing(
            Conversation::getLastMessageAt, Comparator.nullsLast(Comparator.reverseOrder())));

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

    if (SecurityUtils.hasRole("SUPERUSER")) {
      return;
    }

    if (conversation.isSupport()) {
      if (userId.equals(conversation.getRequesterUserId())) {
        return;
      }
      throw new RuntimeException("Unauthorized access to conversation");
    }

    Architect architect = architectRepository.findByUserId(userId).orElse(null);
    Client client = clientRepository.findByUserId(userId).orElse(null);

    boolean hasAccess =
        (architect != null && architect.getId().equals(conversation.getArchitectId()))
            || (client != null && client.getId().equals(conversation.getClientId()));

    if (!hasAccess) {
      throw new RuntimeException("Unauthorized access to conversation");
    }
  }

  public ConversationResponse mapToResponse(Conversation conversation) {
    if (conversation.isSupport()) {
      return mapSupportConversationToResponse(conversation);
    }
    return mapProjectConversationToResponse(conversation);
  }

  private ConversationResponse mapProjectConversationToResponse(Conversation conversation) {
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
      if (lastMessage.getSenderType() == SenderType.SUPERUSER) {
        User su = userRepository.findById(lastMessage.getSenderUserId()).orElse(null);
        senderName = su != null ? su.getFirstName() + " " + su.getLastName() : "Support";
      } else {
        senderName =
            lastMessage.getSenderUserId().equals(architect.getUser().getId())
                ? architectName
                : clientName;
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
        .itSupportRequested(conversation.getItSupportRequested())
        .status(conversation.getStatus())
        .unreadCount(unreadCount)
        .lastMessage(lastMessageResponse)
        .lastMessageAt(conversation.getLastMessageAt())
        .createdAt(conversation.getCreatedAt())
        .build();
  }

  private ConversationResponse mapSupportConversationToResponse(Conversation conversation) {
    Long userId = SecurityUtils.getCurrentUserId();

    User requester = userRepository.findById(conversation.getRequesterUserId()).orElse(null);
    String requesterName =
        requester != null ? requester.getFirstName() + " " + requester.getLastName() : "Unknown";

    Integer unreadCount = messageRepository.countUnreadMessages(conversation.getId(), userId);

    Message lastMessage =
        messageRepository
            .findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId())
            .orElse(null);

    MessageResponse lastMessageResponse = null;
    if (lastMessage != null) {
      User sender = userRepository.findById(lastMessage.getSenderUserId()).orElse(null);
      String senderName =
          sender != null ? sender.getFirstName() + " " + sender.getLastName() : "Unknown";

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
        .requesterUserId(conversation.getRequesterUserId())
        .requesterName(requesterName)
        .status(conversation.getStatus())
        .unreadCount(unreadCount)
        .lastMessage(lastMessageResponse)
        .lastMessageAt(conversation.getLastMessageAt())
        .createdAt(conversation.getCreatedAt())
        .build();
  }
}
