package com.rumantra.payment.domain;

import com.rumantra.shared.domain.StatusLogEntry;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rmtr_token_purchase_status_log")
public class TokenPurchaseStatusLog extends StatusLogEntry {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "token_purchase_id", nullable = false)
  private TokenPurchase tokenPurchase;
}
