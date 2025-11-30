package com.rumantra.notification.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class ProjectValidatedEvent extends ApplicationEvent {

  private final Long projectId;
  private final Long clientId;
  private final String projectTitle;
  private final Boolean isValid;
  private final Long validatedBySuperuserId;
  private final String validationNotes;

  public ProjectValidatedEvent(
      Object source,
      Long projectId,
      Long clientId,
      String projectTitle,
      Boolean isValid,
      Long validatedBySuperuserId,
      String validationNotes) {
    super(source);
    this.projectId = projectId;
    this.clientId = clientId;
    this.projectTitle = projectTitle;
    this.isValid = isValid;
    this.validatedBySuperuserId = validatedBySuperuserId;
    this.validationNotes = validationNotes;
  }
}
