package com.rumantra.client.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.client.dto.ClientDto;
import com.rumantra.client.dto.ClientSignupRequestDto;
import com.rumantra.client.service.ClientService;
import com.rumantra.shared.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
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
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.<ClientDto>builder()
                  .success(false)
                  .message("An error occurred during registration")
                  .build());
    }
  }

  //    @PutMapping("/profile")
  //    public ResponseEntity<ApiResponse<ArchitectDto>> updateProfile(
  //            @Valid @RequestBody UpdateArchitectDto updateRequest, Authentication authentication)
  // {
  //        try {
  //            // Get user ID from authentication
  //            Long userId = getUserIdFromAuthentication(authentication);
  //
  //            ArchitectDto updatedArchitect = clientService.updateArchitect(userId,
  // updateRequest);
  //            return ResponseEntity.ok(
  //                    ApiResponse.<ArchitectDto>builder()
  //                            .success(true)
  //                            .message("Profile updated successfully!")
  //                            .data(updatedArchitect)
  //                            .build());
  //        } catch (IllegalArgumentException e) {
  //            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
  //
  // .body(ApiResponse.<ArchitectDto>builder().success(false).message(e.getMessage()).build());
  //        } catch (Exception e) {
  //            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
  //                    .body(
  //                            ApiResponse.<ArchitectDto>builder()
  //                                    .success(false)
  //                                    .message("An error occurred while updating profile")
  //                                    .build());
  //        }
  //    }
  //
  //    @GetMapping("/profile")
  //    public ResponseEntity<ApiResponse<ArchitectDto>> getProfile(Authentication authentication) {
  //        try {
  //            // Get user ID from authentication
  //            Long userId = getUserIdFromAuthentication(authentication);
  //
  //            ArchitectDto architect = clientService.getArchitectByUserId(userId);
  //            return ResponseEntity.ok(
  //                    ApiResponse.<ArchitectDto>builder()
  //                            .success(true)
  //                            .message("Profile retrieved successfully!")
  //                            .data(architect)
  //                            .build());
  //        } catch (Exception e) {
  //            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
  //                    .body(
  //                            ApiResponse.<ArchitectDto>builder()
  //                                    .success(false)
  //                                    .message("An error occurred while retrieving profile")
  //                                    .build());
  //        }
  //    }
  //
  //    private Long getUserIdFromAuthentication(Authentication authentication) {
  //        if (authentication == null || !authentication.isAuthenticated()) {
  //            throw new IllegalStateException("User is not authenticated");
  //        }
  //
  //        // Extract UserPrincipal from authentication
  //        if (authentication.getPrincipal() instanceof UserPrincipal) {
  //            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
  //            return userPrincipal.getId();
  //        }
  //
  //        throw new IllegalStateException("Unable to extract user ID from authentication");
  //    }
}
