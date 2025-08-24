package com.rumantra.architect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

  @NotBlank(message = "Username or email is required")
  private String usernameOrEmail;

  @NotBlank(message = "Password is required")
  private String password;
}
