package com.rumantra.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkedInUserInfoDto {
  private String email;
  private String firstName;
  private String lastName;
  private String profilePicture;
  private String id;
  private boolean emailVerified;
}
