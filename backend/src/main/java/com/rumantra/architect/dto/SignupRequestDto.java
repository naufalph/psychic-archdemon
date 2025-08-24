package com.rumantra.architect.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDto {

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  @Pattern(
      regexp = "^[a-zA-Z0-9_]+$",
      message = "Username can only contain letters, numbers, and underscores")
  private String userName;

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

  @NotBlank(message = "Company name is required")
  @Size(max = 255, message = "Company name must not exceed 255 characters")
  private String companyName;

  @Size(max = 255, message = "Company site must not exceed 255 characters")
  @Pattern(
      regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/.*)?$",
      message = "Company site must be a valid URL")
  private String companySite;

  @NotBlank(message = "Contact name is required")
  @Size(max = 255, message = "Contact name must not exceed 255 characters")
  private String contactName;

  @NotBlank(message = "KTP number is required")
  @Pattern(regexp = "^[0-9]{16}$", message = "KTP number must be exactly 16 digits")
  private String ktpNum;

  @NotBlank(message = "NPWP is required")
  @Pattern(regexp = "^[0-9]{15,16}$", message = "NPWP must be 15 or 16 digits")
  private String npwp;
}
