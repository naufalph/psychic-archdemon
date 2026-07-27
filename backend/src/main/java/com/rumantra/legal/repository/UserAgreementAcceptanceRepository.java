package com.rumantra.legal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.legal.domain.UserAgreementAcceptance;

@Repository
public interface UserAgreementAcceptanceRepository
    extends JpaRepository<UserAgreementAcceptance, Long> {

  List<UserAgreementAcceptance> findByUserId(Long userId);
}
