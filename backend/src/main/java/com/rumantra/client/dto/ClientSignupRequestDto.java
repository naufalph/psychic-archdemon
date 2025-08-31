package com.rumantra.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSignupRequestDto {

  @NotBlank(message = "UUID is required")
  private long userId;

  @Pattern(regexp = "^(\\+62|62)?0?[0-9]{9,11}$", message = "Phone number must be valid")
  private String phoneNumber;

  @Pattern(regexp = "^[0-9]{16}$", message = "KTP number must be exactly 16 digits")
  private String ktpNum;
}
