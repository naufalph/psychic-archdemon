package com.rumantra.user.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthResponseDto {

  private String token;
  @Builder.Default private String type = "Bearer";
  private Long id;
  private String email;
  private String firstName;
  private String lastName;
  private List<String> registeredRoles;
  private Boolean needsArchitectOnboarding;
  private Boolean needsClientOnboarding;
  private String lastLoginRole;
}
