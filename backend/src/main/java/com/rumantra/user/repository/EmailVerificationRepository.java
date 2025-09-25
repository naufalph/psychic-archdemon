package com.rumantra.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rumantra.user.domain.EmailVerification;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

  Optional<EmailVerification> findByToken(String token);

  Optional<EmailVerification> findByUserIdAndVerifiedFalse(Long userId);

  @Modifying
  @Query("DELETE FROM EmailVerification e WHERE e.userId = :userId")
  void deleteByUserId(@Param("userId") Long userId);

  @Modifying
  @Query("DELETE FROM EmailVerification e WHERE e.expiry < CURRENT_TIMESTAMP")
  void deleteExpiredTokens();
}
