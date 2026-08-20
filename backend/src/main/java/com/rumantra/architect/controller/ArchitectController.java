package com.rumantra.architect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.rumantra.architect.dto.*;
import com.rumantra.architect.service.ArchitectService;
import com.rumantra.architect.service.OtpService;
import com.rumantra.security.UserPrincipal;
import com.rumantra.shared.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rmtr/architects")
@RequiredArgsConstructor
@Slf4j
public class ArchitectController {

  private final ArchitectService architectService;
  private final OtpService otpService;

  @PutMapping("/profile")
  public ResponseEntity<ApiResponse<ArchitectDto>> updateProfile(
      @Valid @RequestBody UpdateArchitectDto updateRequest, Authentication authentication) {
    try {
      // Get user ID from authentication
      Long userId = getUserIdFromAuthentication(authentication);

      ArchitectDto updatedArchitect = architectService.updateArchitect(userId, updateRequest);
      return ResponseEntity.ok(
          ApiResponse.<ArchitectDto>builder()
              .success(true)
              .message("Profile updated successfully!")
              .data(updatedArchitect)
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.<ArchitectDto>builder().success(false).message(e.getMessage()).build());
    } catch (Exception e) {
      log.error("Failed to update architect profile", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ArchitectDto>builder()
                  .success(false)
                  .message("An error occurred while updating profile")
                  .build());
    }
  }

  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<ArchitectDto>> getProfile(Authentication authentication) {
    try {
      // Get user ID from authentication
      Long userId = getUserIdFromAuthentication(authentication);

      ArchitectDto architect = architectService.getArchitectByUserId(userId);
      return ResponseEntity.ok(
          ApiResponse.<ArchitectDto>builder()
              .success(true)
              .message("Profile retrieved successfully!")
              .data(architect)
              .build());
    } catch (Exception e) {
      log.error("Failed to retrieve architect profile", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ArchitectDto>builder()
                  .success(false)
                  .message("An error occurred while retrieving profile")
                  .build());
    }
  }

  @PutMapping("/onboarding-profile")
  public ResponseEntity<ApiResponse<ArchitectDto>> updateOnboardingProfile(
      @Valid @RequestBody UpdateArchitectProfileRequest request, Authentication authentication) {
    try {
      Long userId = getUserIdFromAuthentication(authentication);

      ArchitectDto updatedArchitect = architectService.updateProfile(userId, request);
      return ResponseEntity.ok(
          ApiResponse.<ArchitectDto>builder()
              .success(true)
              .message("Onboarding profile updated successfully!")
              .data(updatedArchitect)
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.<ArchitectDto>builder().success(false).message(e.getMessage()).build());
    } catch (Exception e) {
      log.error("Failed to update architect onboarding profile", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ArchitectDto>builder()
                  .success(false)
                  .message("An error occurred while updating onboarding profile")
                  .build());
    }
  }

  @PostMapping("/phone/send-otp")
  public ResponseEntity<ApiResponse<Void>> sendPhoneOtp(
      @Valid @RequestBody OtpRequestDto request, Authentication authentication) {
    try {
      Long userId = getUserIdFromAuthentication(authentication);
      otpService.sendOtp(userId, request.getPhoneNumber());
      return ResponseEntity.ok(
          ApiResponse.<Void>builder()
              .success(true)
              .message("OTP telah dikirim ke WhatsApp Anda.")
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.<Void>builder().success(false).message(e.getMessage()).build());
    } catch (Exception e) {
      log.error("Failed to send architect OTP", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message("Gagal mengirim OTP. Silakan coba lagi.")
                  .build());
    }
  }

  @PostMapping("/phone/verify-otp")
  public ResponseEntity<ApiResponse<ArchitectDto>> verifyPhoneOtp(
      @Valid @RequestBody OtpVerifyDto request, Authentication authentication) {
    try {
      Long userId = getUserIdFromAuthentication(authentication);
      otpService.verifyOtp(userId, request.getPhoneNumber(), request.getCode());
      ArchitectDto updatedArchitect = architectService.getArchitectByUserId(userId);
      return ResponseEntity.ok(
          ApiResponse.<ArchitectDto>builder()
              .success(true)
              .message("Nomor HP berhasil diverifikasi.")
              .data(updatedArchitect)
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.<ArchitectDto>builder().success(false).message(e.getMessage()).build());
    } catch (Exception e) {
      log.error("Architect OTP verification failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ArchitectDto>builder()
                  .success(false)
                  .message("Verifikasi OTP gagal. Silakan coba lagi.")
                  .build());
    }
  }

  private Long getUserIdFromAuthentication(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("User is not authenticated");
    }

    // Extract UserPrincipal from authentication
    if (authentication.getPrincipal() instanceof UserPrincipal) {
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      return userPrincipal.getId();
    }

    throw new IllegalStateException("Unable to extract user ID from authentication");
  }
}
