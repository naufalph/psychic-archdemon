package com.rumantra.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequestDto {

  @NotBlank(message = "Username or email is required")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @Builder.Default private String role = "ARCHITECT";
}
