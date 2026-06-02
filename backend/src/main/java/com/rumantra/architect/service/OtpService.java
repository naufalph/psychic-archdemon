package com.rumantra.architect.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.domain.OtpVerification;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.architect.repository.OtpVerificationRepository;
import com.rumantra.integration.wablas.WablasService;
import com.rumantra.shared.exception.ResourceNotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

  private static final int OTP_EXPIRY_MINUTES = 10;
  private static final SecureRandom RANDOM = new SecureRandom();

  private final OtpVerificationRepository otpRepository;
  private final ArchitectRepository architectRepository;
  private final WablasService wablasService;

  @Transactional
  public void sendOtp(Long userId, String phoneNumber) {
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Architect not found for user ID: " + userId));

    otpRepository.deleteByArchitectIdAndPhoneNumber(architect.getId(), phoneNumber);

    String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

    OtpVerification record =
        OtpVerification.builder()
            .architect(architect)
            .phoneNumber(phoneNumber)
            .otpCode(otp)
            .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
            .build();

    otpRepository.save(record);

    String message =
        String.format(
            "Kode OTP Rumantra Anda: *%s*%nBerlaku %d menit. Jangan bagikan kode ini kepada siapapun.",
            otp, OTP_EXPIRY_MINUTES);

    wablasService.sendWhatsAppMessage(phoneNumber, message);
  }

  @Transactional
  public void verifyOtp(Long userId, String phoneNumber, String code) {
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Architect not found for user ID: " + userId));

    OtpVerification record =
        otpRepository
            .findFirstByArchitectIdAndPhoneNumberAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                architect.getId(), phoneNumber, LocalDateTime.now())
            .orElseThrow(
                () -> new IllegalArgumentException("OTP tidak valid atau sudah kadaluarsa."));

    if (!record.getOtpCode().equals(code)) {
      throw new IllegalArgumentException("OTP tidak valid atau sudah kadaluarsa.");
    }

    record.setUsed(true);
    otpRepository.save(record);

    architect.setPhoneNumber(phoneNumber);
    architect.setPhoneVerified(true);
    architectRepository.save(architect);
  }
}
