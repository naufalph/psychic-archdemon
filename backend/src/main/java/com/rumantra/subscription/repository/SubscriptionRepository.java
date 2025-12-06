package com.rumantra.subscription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.subscription.domain.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  Optional<Subscription> findByArchitectIdAndIsActive(Long architectId, Boolean isActive);

  List<Subscription> findByArchitectIdOrderByStartDateDesc(Long architectId);
}
