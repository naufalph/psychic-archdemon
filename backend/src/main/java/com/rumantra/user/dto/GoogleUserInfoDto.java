package com.rumantra.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleUserInfoDto {
  private String email;
  private String name;
  private String picture;
  private String locale;
  private boolean emailVerified;
}
