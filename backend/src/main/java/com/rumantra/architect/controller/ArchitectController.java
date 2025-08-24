package com.rumantra.architect.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.rumantra.architect.dto.*;
import com.rumantra.architect.service.ArchitectService;
import com.rumantra.security.UserPrincipal;
import com.rumantra.shared.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/architects")
@RequiredArgsConstructor
public class ArchitectController {

  private final ArchitectService architectService;

  @PostMapping("/signup")
  public ResponseEntity<ApiResponse<ArchitectDto>> signup(
      @Valid @RequestBody SignupRequestDto signupRequest) {
    try {
      ArchitectDto architect = architectService.signup(signupRequest);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.<ArchitectDto>builder()
                  .success(true)
                  .message("Architect registered successfully!")
                  .data(architect)
                  .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.<ArchitectDto>builder().success(false).message(e.getMessage()).build());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ArchitectDto>builder()
                  .success(false)
                  .message("An error occurred during registration")
                  .build());
    }
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponseDto>> login(
      @Valid @RequestBody LoginRequestDto loginRequest, HttpServletRequest request) {
    try {
      AuthResponseDto authResponse = architectService.login(loginRequest);
      request.getSession().setAttribute("architectDto", authResponse.getArchitect());
      return ResponseEntity.ok(
          ApiResponse.<AuthResponseDto>builder()
              .success(true)
              .message("Login successful!")
              .data(authResponse)
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              ApiResponse.<AuthResponseDto>builder()
                  .success(false)
                  .message(e.getMessage())
                  .build());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<AuthResponseDto>builder()
                  .success(false)
                  .message("An error occurred during login")
                  .build());
    }
  }

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
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ArchitectDto>builder()
                  .success(false)
                  .message("An error occurred while retrieving profile")
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
