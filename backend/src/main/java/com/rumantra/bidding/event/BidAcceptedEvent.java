package com.rumantra.bidding.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class BidAcceptedEvent extends ApplicationEvent {

  private final Long bidId;
  private final Long projectId;
  private final String projectTitle;
  private final Long winningArchitectUserId;
  private final String winningArchitectEmail;
  private final String winningArchitectName;
  private final List<RejectedArchitect> rejectedArchitects;

  public BidAcceptedEvent(
      Object source,
      Long bidId,
      Long projectId,
      String projectTitle,
      Long winningArchitectUserId,
      String winningArchitectEmail,
      String winningArchitectName,
      List<RejectedArchitect> rejectedArchitects) {
    super(source);
    this.bidId = bidId;
    this.projectId = projectId;
    this.projectTitle = projectTitle;
    this.winningArchitectUserId = winningArchitectUserId;
    this.winningArchitectEmail = winningArchitectEmail;
    this.winningArchitectName = winningArchitectName;
    this.rejectedArchitects = rejectedArchitects;
  }

  @Getter
  public static class RejectedArchitect {
    private final Long userId;
    private final String email;
    private final String name;

    public RejectedArchitect(Long userId, String email, String name) {
      this.userId = userId;
      this.email = email;
      this.name = name;
    }
  }
}
