package com.rumantra.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.payment.domain.TokenPurchaseStatusLog;

/** Append-only: insert and read only. The database rejects UPDATE, DELETE and TRUNCATE. */
@Repository
public interface TokenPurchaseStatusLogRepository
    extends JpaRepository<TokenPurchaseStatusLog, Long> {

  List<TokenPurchaseStatusLog> findByTokenPurchaseIdOrderByCreatedAtAsc(Long tokenPurchaseId);
}
