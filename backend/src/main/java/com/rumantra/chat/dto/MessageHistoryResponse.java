package com.rumantra.chat.dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageHistoryResponse {

  private List<MessageResponse> messages;
  private Integer currentPage;
  private Integer totalPages;
  private Long totalMessages;
  private Boolean hasMore;
}
