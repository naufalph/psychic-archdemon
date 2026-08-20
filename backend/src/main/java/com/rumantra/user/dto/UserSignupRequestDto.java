package com.rumantra.user.dto;

import java.util.List;

import com.rumantra.legal.dto.AcceptanceRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequestDto {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_-]).*$",
      message =
          "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
  private String password;

  private String firstName;
  private String lastName;

  @NotBlank(message = "Role is required") // this from FE to create architect / client profile
  private String role;

  @NotEmpty(message = "Acceptance of Terms & Conditions and Privacy Policy is required")
  @Valid
  private List<AcceptanceRequest> acceptances;

  /**
   * Optional: links a landing mini-form brief to this account so it survives email verification.
   */
  private String landingBriefToken;
}
