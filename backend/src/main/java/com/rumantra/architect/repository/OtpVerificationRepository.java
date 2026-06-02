package com.rumantra.architect.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.rumantra.architect.domain.OtpVerification;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

  Optional<OtpVerification>
      findFirstByArchitectIdAndPhoneNumberAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
          Long architectId, String phoneNumber, LocalDateTime now);

  @Modifying
  @Query(
      "DELETE FROM OtpVerification o WHERE o.architect.id = :architectId AND o.phoneNumber = :phoneNumber")
  void deleteByArchitectIdAndPhoneNumber(Long architectId, String phoneNumber);
}
