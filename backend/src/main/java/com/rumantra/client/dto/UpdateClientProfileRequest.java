package com.rumantra.client.dto;

import com.rumantra.shared.constants.ErrorCodes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateClientProfileRequest {

  private String firstName;

  private String lastName;

  @NotBlank(message = ErrorCodes.PHONE_REQUIRED)
  @Pattern(regexp = "^\\+?[0-9\\s-]{10,16}$", message = ErrorCodes.PHONE_INVALID_FORMAT)
  private String phoneNumber;

  @Pattern(regexp = "^\\d{16}$", message = ErrorCodes.KTP_INVALID_FORMAT)
  private String ktpNum;
}
