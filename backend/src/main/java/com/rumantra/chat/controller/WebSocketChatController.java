package com.rumantra.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.rumantra.chat.dto.SendMessageRequest;
import com.rumantra.chat.service.MessageService;

@Controller
public class WebSocketChatController {

  @Autowired private MessageService messageService;

  @MessageMapping("/chat.send")
  public void sendMessage(
      @Payload SendMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
    messageService.sendMessage(request);
  }

  @MessageMapping("/chat.read")
  public void markAsRead(@Payload Long messageId) {
    messageService.markAsRead(messageId);
  }
}
