package com.rumantra.client.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.client.dto.ClientDto;
import com.rumantra.client.dto.ClientSignupRequestDto;
import com.rumantra.client.dto.UpdateClientProfileRequest;
import com.rumantra.client.service.ClientService;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rmtr/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

  private final ClientService clientService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<ClientDto>> signup(
      @Valid @RequestBody ClientSignupRequestDto signupRequest) {
    try {
      ClientDto clientDto = clientService.register(signupRequest);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.<ClientDto>builder()
                  .success(true)
                  .message("Architect registered successfully!")
                  .data(clientDto)
                  .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.<ClientDto>builder().success(false).message(e.getMessage()).build());
    } catch (Exception e) {
      log.error("Client registration failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ClientDto>builder()
                  .success(false)
                  .message("An error occurred during registration")
                  .build());
    }
  }

  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<ClientDto>> getProfile() {
    try {
      Long userId = SecurityUtils.getCurrentUserId();
      ClientDto client = clientService.getClientByUserId(userId);
      return ResponseEntity.ok(
          ApiResponse.<ClientDto>builder()
              .success(true)
              .message("Profile retrieved successfully!")
              .data(client)
              .build());
    } catch (Exception e) {
      log.error("Failed to retrieve client profile", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ClientDto>builder()
                  .success(false)
                  .message("An error occurred while retrieving profile")
                  .build());
    }
  }

  @PutMapping("/profile")
  public ResponseEntity<ApiResponse<ClientDto>> updateProfile(
      @Valid @RequestBody UpdateClientProfileRequest updateRequest) {
    try {
      Long userId = SecurityUtils.getCurrentUserId();
      ClientDto updatedClient = clientService.updateClientProfile(userId, updateRequest);
      return ResponseEntity.ok(
          ApiResponse.<ClientDto>builder()
              .success(true)
              .message("Profile updated successfully!")
              .data(updatedClient)
              .build());
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.<ClientDto>builder().success(false).message(e.getMessage()).build());
    } catch (Exception e) {
      log.error("Failed to update client profile", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ClientDto>builder()
                  .success(false)
                  .message("An error occurred while updating profile")
                  .build());
    }
  }
}
