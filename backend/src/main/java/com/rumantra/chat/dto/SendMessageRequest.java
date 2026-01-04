package com.rumantra.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

  @NotNull(message = "Conversation ID is required")
  private Long conversationId;

  @NotBlank(message = "Message content cannot be empty")
  private String content;
}
