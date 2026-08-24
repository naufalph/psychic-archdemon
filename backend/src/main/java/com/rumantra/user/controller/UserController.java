package com.rumantra.user.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.rumantra.security.JwtUtils;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.RequestUtils;
import com.rumantra.shared.RumantraConstants;
import com.rumantra.shared.dto.ApiResponse;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.user.dto.ChangePasswordRequestDto;
import com.rumantra.user.dto.UserAuthResponseDto;
import com.rumantra.user.dto.UserDto;
import com.rumantra.user.dto.UserLoginRequestDto;
import com.rumantra.user.dto.UserSignupRequestDto;
import com.rumantra.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rmtr/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final JwtUtils jwtUtils;

  @Value("${app.frontend.url:http://localhost:3001}")
  private String frontendUrl;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<UserAuthResponseDto>> login(
      @Valid @RequestBody UserLoginRequestDto loginRequest) {
    try {
      UserAuthResponseDto authResponse = userService.login(loginRequest);

      // Generate JWT token and add it to the response
      String jwtToken = jwtUtils.generateJwtToken(authResponse.getEmail());
      authResponse.setToken(jwtToken);

      return ResponseEntity.ok(
          ApiResponse.<UserAuthResponseDto>builder()
              .success(true)
              .message("Login successful!")
              .data(authResponse)
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              ApiResponse.<UserAuthResponseDto>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              ApiResponse.<UserAuthResponseDto>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<UserAuthResponseDto>builder()
                  .success(false)
                  .message("An error occurred during login")
                  .build());
    }
  }

  @GetMapping("/oauth2/google")
  public ResponseEntity<String> googleLogin(
      @RequestParam(value = "role", required = false) String role,
      @RequestParam(value = "acceptances", required = false) String acceptances) {
    return ResponseEntity.ok(userService.getGoogleAuthorizationUrl(role, acceptances));
  }

  @GetMapping("/oauth2/callback/google")
  public RedirectView googleCallback(
      @RequestParam("code") String code,
      @RequestParam(value = "state", required = false) String state,
      HttpServletRequest request) {
    try {
      UserAuthResponseDto authResponse = userService.processGoogleCallback(code, state, request);

      String jwtToken = jwtUtils.generateJwtToken(authResponse.getEmail());
      authResponse.setToken(jwtToken);

      String roles = String.join(",", authResponse.getRegisteredRoles());
      String callbackUrl =
          frontendUrl
              + "/auth/callback?"
              + "success=true"
              + "&token="
              + jwtToken
              + "&email="
              + authResponse.getEmail()
              + "&id="
              + authResponse.getId()
              + "&roles="
              + roles
              + "&needsClientOnboarding="
              + (authResponse.getNeedsClientOnboarding() != null
                  ? authResponse.getNeedsClientOnboarding()
                  : "")
              + "&lastLoginRole="
              + (authResponse.getLastLoginRole() != null ? authResponse.getLastLoginRole() : "");

      return new RedirectView(callbackUrl);
    } catch (BusinessException e) {
      // Surface the stable error code (e.g. STALE_TERMS) rather than a free-text
      // message, so the frontend can branch on it reliably.
      String callbackUrl =
          frontendUrl
              + "/auth/callback?"
              + "success=false"
              + "&error="
              + e.getExceptionCode().getCode();

      return new RedirectView(callbackUrl);
    } catch (Exception e) {
      String callbackUrl =
          frontendUrl + "/auth/callback?" + "success=false" + "&error=" + e.getMessage();

      return new RedirectView(callbackUrl);
    }
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<String>> register(
      @Valid @RequestBody UserSignupRequestDto signupRequest, HttpServletRequest request) {
    try {
      String ipAddress = RequestUtils.getClientIp(request);
      String userAgent = RequestUtils.getUserAgent(request);
      UserDto userDto = userService.register(signupRequest, ipAddress, userAgent);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.<String>builder()
                  .success(true)
                  .message(
                      "Registration successful! Please check your email to verify your account.")
                  .data("Verification email sent to: " + userDto.getEmail())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.<String>builder().success(false).message(e.getMessage()).build());
    } catch (BusinessException e) {
      // Let GlobalExceptionHandler map this to its configured HTTP status + errorCode
      // (e.g. STALE_TERMS -> 409) instead of flattening it into a generic 500 below.
      throw e;
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<String>builder()
                  .success(false)
                  .message("An error occurred during registration")
                  .build());
    }
  }

  @GetMapping("/verify-email")
  public ResponseEntity<ApiResponse<UserAuthResponseDto>> verifyEmail(
      @RequestParam("token") String token) {
    try {
      log.info(
          "Verifying email with token: {}",
          token.substring(0, Math.min(10, token.length())) + "...");
      userService.verifyEmail(token);

      return ResponseEntity.ok(
          ApiResponse.<UserAuthResponseDto>builder()
              .success(true)
              .message("Email verified successfully! You can now log in.")
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (IllegalArgumentException e) {
      log.error("Email verification failed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<UserAuthResponseDto>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    } catch (ResourceNotFoundException e) {
      log.error("User not found during email verification: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<UserAuthResponseDto>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    } catch (Exception e) {
      log.error("Error during email verification: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<UserAuthResponseDto>builder()
                  .success(false)
                  .message("An error occurred during email verification: " + e.getMessage())
                  .build());
    }
  }

  @PostMapping("/resend-verification")
  public ResponseEntity<ApiResponse<String>> resendVerificationEmail(
      @RequestParam("email") String email) {
    try {
      log.info("Resending verification email to: {}", email);
      userService.resendVerificationEmail(email);
      return ResponseEntity.ok(
          ApiResponse.<String>builder()
              .success(true)
              .message("Verification email resent successfully!")
              .data("Please check your email for the new verification link")
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (ResourceNotFoundException e) {
      log.error("User not found for email: {}", email);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.<String>builder().success(false).message(e.getMessage()).build());
    } catch (IllegalArgumentException e) {
      log.error("Invalid argument for resend verification: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.<String>builder().success(false).message(e.getMessage()).build());
    } catch (Exception e) {
      log.error("Error resending verification email to {}: {}", email, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<String>builder()
                  .success(false)
                  .message(
                      "An error occurred while resending verification email: " + e.getMessage())
                  .build());
    }
  }

  @GetMapping("/oauth2/linkedin")
  public ResponseEntity<String> linkedinLogin(
      @RequestParam(value = "role", required = false) String role,
      @RequestParam(value = "acceptances", required = false) String acceptances) {
    return ResponseEntity.ok(userService.getLinkedInAuthorizationUrl(role, acceptances));
  }

  @GetMapping("/oauth2/callback/linkedin")
  public RedirectView linkedinCallback(
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "state", required = false) String state,
      @RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "error_description", required = false) String errorDescription,
      HttpServletRequest request) {
    try {
      if (error != null) {
        String errorMsg = errorDescription != null ? errorDescription : error;
        throw new IllegalArgumentException("LinkedIn OAuth error: " + errorMsg);
      }

      if (code == null || code.trim().isEmpty()) {
        throw new IllegalArgumentException("Authorization code is missing from LinkedIn callback");
      }

      UserAuthResponseDto authResponse = userService.processLinkedInCallback(code, state, request);

      String jwtToken = jwtUtils.generateJwtToken(authResponse.getEmail());
      authResponse.setToken(jwtToken);

      String roles = String.join(",", authResponse.getRegisteredRoles());
      String callbackUrl =
          frontendUrl
              + "/auth/callback?"
              + "success=true"
              + "&token="
              + jwtToken
              + "&email="
              + authResponse.getEmail()
              + "&id="
              + authResponse.getId()
              + "&roles="
              + roles
              + "&needsClientOnboarding="
              + (authResponse.getNeedsClientOnboarding() != null
                  ? authResponse.getNeedsClientOnboarding()
                  : "")
              + "&lastLoginRole="
              + (authResponse.getLastLoginRole() != null ? authResponse.getLastLoginRole() : "");

      return new RedirectView(callbackUrl);
    } catch (BusinessException e) {
      // Surface the stable error code (e.g. STALE_TERMS) rather than a free-text
      // message, so the frontend can branch on it reliably.
      String callbackUrl =
          frontendUrl
              + "/auth/callback?"
              + "success=false"
              + "&error="
              + e.getExceptionCode().getCode();

      return new RedirectView(callbackUrl);
    } catch (Exception e) {
      String callbackUrl =
          frontendUrl + "/auth/callback?" + "success=false" + "&error=" + e.getMessage();

      return new RedirectView(callbackUrl);
    }
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
    try {
      UserDto userDto = userService.getCurrentUser();

      return ResponseEntity.ok(
          ApiResponse.<UserDto>builder()
              .success(true)
              .data(userDto)
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              ApiResponse.<UserDto>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<UserDto>builder()
                  .success(false)
                  .message("Failed to fetch user data")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/me/activate-role")
  public ResponseEntity<ApiResponse<UserDto>> activateRole(@RequestParam("role") String role) {
    try {
      // Validate role parameter
      if (!RumantraConstants.ARCH_ROLE.equals(role)
          && !RumantraConstants.CLIENT_ROLE.equals(role)) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ApiResponse.<UserDto>builder()
                    .success(false)
                    .message(
                        "Invalid role. Must be '"
                            + RumantraConstants.ARCH_ROLE
                            + "' or '"
                            + RumantraConstants.CLIENT_ROLE
                            + "'")
                    .timestamp(LocalDateTime.now().toString())
                    .build());
      }

      Long userId = SecurityUtils.getCurrentUserId();
      UserDto userDto = userService.activateRole(userId, role);

      return ResponseEntity.ok(
          ApiResponse.<UserDto>builder()
              .success(true)
              .message("Role '" + role + "' activated successfully!")
              .data(userDto)
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<UserDto>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<UserDto>builder()
                  .success(false)
                  .message("An error occurred while activating role")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PutMapping("/me/last-login-role")
  public ResponseEntity<ApiResponse<String>> updateLastLoginRole(
      @RequestParam("role") String role) {
    try {
      if (!RumantraConstants.ARCH_ROLE.equals(role)
          && !RumantraConstants.CLIENT_ROLE.equals(role)) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ApiResponse.<String>builder()
                    .success(false)
                    .message("Invalid role. Must be 'ARCHITECT' or 'CLIENT'")
                    .timestamp(LocalDateTime.now().toString())
                    .build());
      }

      Long userId = SecurityUtils.getCurrentUserId();
      userService.updateLastLoginRole(userId, role);

      return ResponseEntity.ok(
          ApiResponse.<String>builder()
              .success(true)
              .message("Last login role updated successfully")
              .data(role)
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<String>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<String>builder()
                  .success(false)
                  .message("An error occurred while updating last login role")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }

  @PostMapping("/me/change-password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @Valid @RequestBody ChangePasswordRequestDto request) {
    try {
      Long userId = SecurityUtils.getCurrentUserId();
      userService.changePassword(userId, request);

      return ResponseEntity.ok(
          ApiResponse.<Void>builder()
              .success(true)
              .message("Password changed successfully!")
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message(e.getMessage())
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    } catch (Exception e) {
      log.error("Failed to change password", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<Void>builder()
                  .success(false)
                  .message("An error occurred while changing password")
                  .timestamp(LocalDateTime.now().toString())
                  .build());
    }
  }
}
