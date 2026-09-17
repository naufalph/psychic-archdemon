package com.rumantra.project.domain;

import org.hibernate.annotations.Immutable;

import com.rumantra.shared.domain.StatusLogEntry;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Immutable
@Entity
@Table(name = "rmtr_phase_disbursement_status_log")
public class PhaseDisbursementStatusLog extends StatusLogEntry {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "disbursement_id", nullable = false)
  private PhaseDisbursement disbursement;
}
