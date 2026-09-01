package com.rumantra.subscription.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.subscription.domain.SubscriptionStatusLog;

/** Append-only: insert and read only. The database rejects UPDATE, DELETE and TRUNCATE. */
@Repository
public interface SubscriptionStatusLogRepository
    extends JpaRepository<SubscriptionStatusLog, Long> {

  List<SubscriptionStatusLog> findBySubscriptionIdOrderByCreatedAtAsc(Long subscriptionId);
}
