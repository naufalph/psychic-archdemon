package com.rumantra.architect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequestDto {

  @NotBlank(message = "Phone number is required")
  private String phoneNumber;
}
