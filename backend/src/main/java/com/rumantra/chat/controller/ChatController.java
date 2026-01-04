package com.rumantra.chat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.chat.dto.ConversationResponse;
import com.rumantra.chat.dto.MessageHistoryResponse;
import com.rumantra.chat.dto.MessageResponse;
import com.rumantra.chat.dto.SendMessageRequest;
import com.rumantra.chat.service.ConversationService;
import com.rumantra.chat.service.MessageService;
import com.rumantra.shared.dto.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

  @Autowired private ConversationService conversationService;

  @Autowired private MessageService messageService;

  @GetMapping("/conversations")
  public ResponseEntity<ApiResponse<List<ConversationResponse>>> getMyConversations() {
    List<ConversationResponse> conversations = conversationService.getMyConversations();
    return ResponseEntity.ok(ApiResponse.success(conversations));
  }

  @GetMapping("/conversations/{conversationId}")
  public ResponseEntity<ApiResponse<ConversationResponse>> getConversation(
      @PathVariable Long conversationId) {
    ConversationResponse conversation = conversationService.getConversationById(conversationId);
    return ResponseEntity.ok(ApiResponse.success(conversation));
  }

  @GetMapping("/conversations/{conversationId}/messages")
  public ResponseEntity<ApiResponse<MessageHistoryResponse>> getMessageHistory(
      @PathVariable Long conversationId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    MessageHistoryResponse history = messageService.getMessageHistory(conversationId, page, size);
    return ResponseEntity.ok(ApiResponse.success(history));
  }

  @PostMapping("/messages")
  public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
      @Valid @RequestBody SendMessageRequest request) {
    MessageResponse message = messageService.sendMessage(request);
    return ResponseEntity.ok(ApiResponse.success(message));
  }

  @PostMapping("/conversations/{conversationId}/upload")
  public ResponseEntity<ApiResponse<MessageResponse>> uploadFile(
      @PathVariable Long conversationId, @RequestParam("file") MultipartFile file) {
    MessageResponse message = messageService.sendFileMessage(conversationId, file);
    return ResponseEntity.ok(ApiResponse.success(message));
  }

  @PutMapping("/messages/{messageId}/read")
  public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long messageId) {
    messageService.markAsRead(messageId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping("/conversations/{conversationId}/read-all")
  public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long conversationId) {
    messageService.markAllAsRead(conversationId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping("/conversations/{conversationId}/archive")
  public ResponseEntity<ApiResponse<Void>> archiveConversation(@PathVariable Long conversationId) {
    conversationService.archiveConversation(conversationId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
