package com.rumantra.subscription.domain;

import org.hibernate.annotations.Immutable;

import com.rumantra.shared.domain.StatusLogEntry;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Immutable
@Entity
@Table(name = "rmtr_subscription_status_log")
public class SubscriptionStatusLog extends StatusLogEntry {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id", nullable = false)
  private Subscription subscription;
}
