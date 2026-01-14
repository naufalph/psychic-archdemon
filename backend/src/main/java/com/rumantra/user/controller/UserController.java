package com.rumantra.user.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.rumantra.security.JwtUtils;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.RumantraConstants;
import com.rumantra.shared.dto.ApiResponse;
import com.rumantra.user.dto.UserAuthResponseDto;
import com.rumantra.user.dto.UserDto;
import com.rumantra.user.dto.UserLoginRequestDto;
import com.rumantra.user.dto.UserSignupRequestDto;
import com.rumantra.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
  public ResponseEntity<String> googleLogin() {
    // Return the Google OAuth2 authorization URL
    return ResponseEntity.ok(userService.getGoogleAuthorizationUrl());
  }

  @GetMapping("/oauth2/callback/google")
  public RedirectView googleCallback(@RequestParam("code") String code) {
    try {
      UserAuthResponseDto authResponse = userService.processGoogleCallback(code);

      // Generate JWT token and add it to the response
      String jwtToken = jwtUtils.generateJwtToken(authResponse.getEmail());
      authResponse.setToken(jwtToken);

      // Redirect to frontend with success and token
      String roles = String.join(",", authResponse.getRegisteredRoles());
      String callbackUrl =
          frontendUrl
              + "?"
              + "success=true"
              + "&token="
              + jwtToken
              + "&email="
              + authResponse.getEmail()
              + "&id="
              + authResponse.getId()
              + "&roles="
              + roles;

      return new RedirectView(callbackUrl);
    } catch (Exception e) {
      // Redirect to frontend with error
      String callbackUrl =
          frontendUrl + "/auth/callback?" + "success=false" + "&error=" + e.getMessage();

      return new RedirectView(callbackUrl);
    }
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<String>> register(
      @Valid @RequestBody UserSignupRequestDto signupRequest) {
    try {
      UserDto userDto = userService.register(signupRequest);
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
      userService.verifyEmail(token);

      return ResponseEntity.ok(
          ApiResponse.<UserAuthResponseDto>builder()
              .success(true)
              .message("Email verified successfully! You can now log in.")
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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
                  .message("An error occurred during email verification")
                  .build());
    }
  }

  @PostMapping("/resend-verification")
  public ResponseEntity<ApiResponse<String>> resendVerificationEmail(
      @RequestParam("email") String email) {
    try {
      userService.resendVerificationEmail(email);
      return ResponseEntity.ok(
          ApiResponse.<String>builder()
              .success(true)
              .message("Verification email resent successfully!")
              .data("Please check your email for the new verification link")
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.<String>builder().success(false).message(e.getMessage()).build());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<String>builder()
                  .success(false)
                  .message("An error occurred while resending verification email")
                  .build());
    }
  }

  @GetMapping("/oauth2/linkedin")
  public ResponseEntity<String> linkedinLogin() {
    // Return the LinkedIn OAuth2 authorization URL
    return ResponseEntity.ok(userService.getLinkedInAuthorizationUrl());
  }

  @GetMapping("/oauth2/callback/linkedin")
  public RedirectView linkedinCallback(
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "error_description", required = false) String errorDescription) {
    try {
      // Check if LinkedIn sent an error instead of code
      if (error != null) {
        String errorMsg = errorDescription != null ? errorDescription : error;
        throw new IllegalArgumentException("LinkedIn OAuth error: " + errorMsg);
      }

      // Check if code is present
      if (code == null || code.trim().isEmpty()) {
        throw new IllegalArgumentException("Authorization code is missing from LinkedIn callback");
      }

      UserAuthResponseDto authResponse = userService.processLinkedInCallback(code);

      // Generate JWT token and add it to the response
      String jwtToken = jwtUtils.generateJwtToken(authResponse.getEmail());
      authResponse.setToken(jwtToken);

      // Redirect to frontend with success and token
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
              + roles;

      return new RedirectView(callbackUrl);
    } catch (Exception e) {
      // Redirect to frontend with error
      String callbackUrl =
          frontendUrl + "/auth/callback?" + "success=false" + "&error=" + e.getMessage();

      return new RedirectView(callbackUrl);
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
}
