package com.rumantra.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
  private Long id;
  private String userName;
  private String email;
  private String firstName;
  private String lastName;
  private boolean isEmailVerified;
  private boolean isActive;
  private java.util.List<String> registeredRoles;
  private Boolean needsArchitectOnboarding;
  private Boolean needsClientOnboarding;
  private String lastLoginRole;
}
