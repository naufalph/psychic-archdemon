package com.rumantra.chat.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupportConversationRequest {
  private Long projectId;
  private Long bidId;
}
