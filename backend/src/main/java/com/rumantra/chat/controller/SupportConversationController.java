package com.rumantra.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.chat.dto.ConversationResponse;
import com.rumantra.chat.dto.SupportConversationRequest;
import com.rumantra.chat.service.SupportConversationService;
import com.rumantra.shared.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/support/conversations")
@RequiredArgsConstructor
public class SupportConversationController {

  private final SupportConversationService supportConversationService;

  @PostMapping
  public ResponseEntity<ApiResponse<ConversationResponse>> createOrGet(
      @RequestBody SupportConversationRequest request) {
    ConversationResponse response =
        supportConversationService.getOrCreate(request.getProjectId(), request.getBidId());
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<ConversationResponse>>> getAllForSuperuser() {
    List<ConversationResponse> conversations = supportConversationService.getAllForSuperuser();
    return ResponseEntity.ok(ApiResponse.success(conversations));
  }
}
