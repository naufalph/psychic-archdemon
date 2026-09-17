package com.rumantra.client.domain;

import org.hibernate.annotations.Immutable;

import com.rumantra.shared.domain.StatusLogEntry;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Immutable
@Entity
@Table(name = "rmtr_project_status_log")
public class ProjectStatusLog extends StatusLogEntry {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;
}
