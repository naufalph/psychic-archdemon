package com.rumantra.architect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyDto {

  @NotBlank(message = "Phone number is required")
  private String phoneNumber;

  @NotBlank(message = "OTP code is required")
  @Size(min = 6, max = 6, message = "OTP code must be 6 digits")
  private String code;
}
