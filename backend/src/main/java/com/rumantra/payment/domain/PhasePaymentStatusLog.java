package com.rumantra.payment.domain;

import com.rumantra.shared.domain.StatusLogEntry;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rmtr_phase_payment_status_log")
public class PhasePaymentStatusLog extends StatusLogEntry {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "phase_payment_id", nullable = false)
  private PhasePayment phasePayment;
}
