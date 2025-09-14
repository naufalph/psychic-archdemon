package com.rumantra.user.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.shared.RumantraConstants;
import com.rumantra.shared.dto.ApiResponse;
import com.rumantra.user.dto.UserAuthResponseDto;
import com.rumantra.user.dto.UserDto;
import com.rumantra.user.dto.UserLoginRequestDto;
import com.rumantra.user.dto.UserSignupRequestDto;
import com.rumantra.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rmtr/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<UserAuthResponseDto>> login(
      @Valid @RequestBody UserLoginRequestDto loginRequest, HttpServletRequest request) {
    try {
      UserAuthResponseDto authResponse = userService.login(loginRequest);
      request.getSession().setAttribute(RumantraConstants.LOGIN_CONTAINER, authResponse);
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
  public ResponseEntity<ApiResponse<UserAuthResponseDto>> googleCallback(
      @RequestParam("code") String code, HttpServletRequest request) {
    try {
      UserAuthResponseDto authResponse = userService.processGoogleCallback(code);
      request.getSession().setAttribute(RumantraConstants.LOGIN_CONTAINER, authResponse);
      return ResponseEntity.ok(
          ApiResponse.<UserAuthResponseDto>builder()
              .success(true)
              .message("Google login successful!")
              .data(authResponse)
              .timestamp(LocalDateTime.now().toString())
              .build());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<UserAuthResponseDto>builder()
                  .success(false)
                  .message("An error occurred during Google login")
                  .build());
    }
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserDto>> register(
      @Valid @RequestBody UserSignupRequestDto signupRequest) {
    try {
      UserDto userDto = userService.register(signupRequest);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.<UserDto>builder()
                  .success(true)
                  .message("Architect registered successfully!")
                  .data(userDto)
                  .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.<UserDto>builder().success(false).message(e.getMessage()).build());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<UserDto>builder()
                  .success(false)
                  .message("An error occurred during registration")
                  .build());
    }
  }
}
