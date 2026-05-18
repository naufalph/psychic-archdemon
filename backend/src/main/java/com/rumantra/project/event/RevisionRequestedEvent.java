package com.rumantra.project.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class RevisionRequestedEvent extends ApplicationEvent {

  private final Long phaseId;
  private final Long projectId;
  private final Long architectUserId;
  private final Long clientUserId;
  private final String projectTitle;
  private final String phaseTitle;
  private final Integer revisionsUsed;
  private final Integer maxRevisions;
  private final String notes;

  public RevisionRequestedEvent(
      Object source,
      Long phaseId,
      Long projectId,
      Long architectUserId,
      Long clientUserId,
      String projectTitle,
      String phaseTitle,
      Integer revisionsUsed,
      Integer maxRevisions,
      String notes) {
    super(source);
    this.phaseId = phaseId;
    this.projectId = projectId;
    this.architectUserId = architectUserId;
    this.clientUserId = clientUserId;
    this.projectTitle = projectTitle;
    this.phaseTitle = phaseTitle;
    this.revisionsUsed = revisionsUsed;
    this.maxRevisions = maxRevisions;
    this.notes = notes;
  }
}
