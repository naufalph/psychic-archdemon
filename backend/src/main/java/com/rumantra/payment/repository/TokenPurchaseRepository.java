package com.rumantra.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.payment.domain.TokenPurchase;

@Repository
public interface TokenPurchaseRepository extends JpaRepository<TokenPurchase, Long> {

  Page<TokenPurchase> findByArchitectIdOrderByCreatedAtDesc(Long architectId, Pageable pageable);

  Optional<TokenPurchase> findByXenditReferenceId(String xenditReferenceId);

  List<TokenPurchase> findByArchitectIdOrderByCreatedAtDesc(Long architectId);
}
