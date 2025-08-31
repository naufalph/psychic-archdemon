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
  private String type = "Bearer";
  private Long id;
  private String email;
  private List<String> registeredRoles;
}
