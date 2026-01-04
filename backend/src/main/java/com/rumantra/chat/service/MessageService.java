package com.rumantra.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.chat.domain.Conversation;
import com.rumantra.chat.domain.Message;
import com.rumantra.chat.domain.MessageFile;
import com.rumantra.chat.domain.MessageType;
import com.rumantra.chat.domain.SenderType;
import com.rumantra.chat.dto.MessageFileResponse;
import com.rumantra.chat.dto.MessageHistoryResponse;
import com.rumantra.chat.dto.MessageResponse;
import com.rumantra.chat.dto.SendMessageRequest;
import com.rumantra.chat.repository.ConversationRepository;
import com.rumantra.chat.repository.MessageFileRepository;
import com.rumantra.chat.repository.MessageRepository;
import com.rumantra.client.domain.Client;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.storage.FileStorageService;

@Service
public class MessageService {

  @Autowired private MessageRepository messageRepository;

  @Autowired private ConversationRepository conversationRepository;

  @Autowired private MessageFileRepository messageFileRepository;

  @Autowired private ArchitectRepository architectRepository;

  @Autowired private ClientRepository clientRepository;

  @Autowired private ConversationService conversationService;

  @Autowired private FileStorageService fileStorageService;

  @Autowired private SimpMessagingTemplate messagingTemplate;

  @Transactional
  public MessageResponse sendMessage(SendMessageRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();

    Conversation conversation =
        conversationRepository
            .findById(request.getConversationId())
            .orElseThrow(() -> new RuntimeException("Conversation not found"));

    conversationService.verifyAccess(conversation);

    SenderType senderType = determineSenderType(userId, conversation);

    Message message =
        Message.builder()
            .conversationId(conversation.getId())
            .senderUserId(userId)
            .senderType(senderType)
            .content(request.getContent())
            .messageType(MessageType.TEXT)
            .isRead(false)
            .build();

    message = messageRepository.save(message);

    conversationService.updateLastMessageTime(conversation.getId());

    MessageResponse response = mapToResponse(message, conversation);

    messagingTemplate.convertAndSend("/topic/conversation." + conversation.getId(), response);

    return response;
  }

  @Transactional
  public MessageResponse sendFileMessage(Long conversationId, MultipartFile file) {
    Long userId = SecurityUtils.getCurrentUserId();

    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));

    conversationService.verifyAccess(conversation);

    SenderType senderType = determineSenderType(userId, conversation);

    String path = "chat/" + conversationId + "/files";
    String fileUrl = fileStorageService.uploadFile(file, path);
    String fileName = file.getOriginalFilename();
    Long fileSize = file.getSize();

    Message message =
        Message.builder()
            .conversationId(conversation.getId())
            .senderUserId(userId)
            .senderType(senderType)
            .content(fileName)
            .messageType(MessageType.FILE)
            .isRead(false)
            .build();

    message = messageRepository.save(message);

    MessageFile messageFile =
        MessageFile.builder()
            .messageId(message.getId())
            .fileName(fileName)
            .fileUrl(fileUrl)
            .fileSize(fileSize)
            .build();

    messageFileRepository.save(messageFile);

    conversationService.updateLastMessageTime(conversation.getId());

    MessageResponse response = mapToResponse(message, conversation);

    messagingTemplate.convertAndSend("/topic/conversation." + conversation.getId(), response);

    return response;
  }

  public MessageHistoryResponse getMessageHistory(Long conversationId, int page, int size) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));

    conversationService.verifyAccess(conversation);

    Pageable pageable = PageRequest.of(page, size);
    Page<Message> messagePage =
        messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

    List<MessageResponse> messages =
        messagePage.getContent().stream()
            .map(msg -> mapToResponse(msg, conversation))
            .collect(Collectors.toList());

    return MessageHistoryResponse.builder()
        .messages(messages)
        .currentPage(page)
        .totalPages(messagePage.getTotalPages())
        .totalMessages(messagePage.getTotalElements())
        .hasMore(messagePage.hasNext())
        .build();
  }

  @Transactional
  public void markAsRead(Long messageId) {
    Message message =
        messageRepository
            .findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));

    Long userId = SecurityUtils.getCurrentUserId();

    if (message.getSenderUserId().equals(userId)) {
      return;
    }

    if (!message.getIsRead()) {
      message.setIsRead(true);
      message.setReadAt(LocalDateTime.now());
      messageRepository.save(message);

      messagingTemplate.convertAndSend(
          "/topic/conversation." + message.getConversationId() + ".read", messageId);
    }
  }

  @Transactional
  public void markAllAsRead(Long conversationId) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));

    conversationService.verifyAccess(conversation);

    Long userId = SecurityUtils.getCurrentUserId();
    int updatedCount = messageRepository.markAllAsRead(conversationId, userId);

    if (updatedCount > 0) {
      messagingTemplate.convertAndSend(
          "/topic/conversation." + conversationId + ".read-all", userId);
    }
  }

  private SenderType determineSenderType(Long userId, Conversation conversation) {
    Architect architect = architectRepository.findByUserId(userId).orElse(null);
    Client client = clientRepository.findByUserId(userId).orElse(null);

    if (architect != null && conversation.getArchitectId().equals(architect.getId())) {
      return SenderType.ARCHITECT;
    }
    if (client != null && conversation.getClientId().equals(client.getId())) {
      return SenderType.CLIENT;
    }

    throw new RuntimeException("User is not part of this conversation");
  }

  private MessageResponse mapToResponse(Message message, Conversation conversation) {
    Architect architect =
        architectRepository
            .findById(conversation.getArchitectId())
            .orElseThrow(() -> new RuntimeException("Architect not found"));

    Client client =
        clientRepository
            .findById(conversation.getClientId())
            .orElseThrow(() -> new RuntimeException("Client not found"));

    String senderName;
    if (message.getSenderUserId().equals(architect.getUser().getId())) {
      senderName = architect.getUser().getFirstName() + " " + architect.getUser().getLastName();
    } else {
      senderName = client.getUser().getFirstName() + " " + client.getUser().getLastName();
    }

    MessageFileResponse fileResponse = null;
    if (message.getMessageType() == MessageType.FILE) {
      MessageFile messageFile = messageFileRepository.findByMessageId(message.getId()).orElse(null);

      if (messageFile != null) {
        fileResponse =
            MessageFileResponse.builder()
                .id(messageFile.getId())
                .fileName(messageFile.getFileName())
                .fileUrl(messageFile.getFileUrl())
                .fileSize(messageFile.getFileSize())
                .build();
      }
    }

    return MessageResponse.builder()
        .id(message.getId())
        .conversationId(message.getConversationId())
        .senderUserId(message.getSenderUserId())
        .senderName(senderName)
        .senderType(message.getSenderType())
        .content(message.getContent())
        .messageType(message.getMessageType())
        .isRead(message.getIsRead())
        .readAt(message.getReadAt())
        .file(fileResponse)
        .createdAt(message.getCreatedAt())
        .build();
  }
}
