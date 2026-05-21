package com.rumantra.admin.dto;

import java.sql.Timestamp;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserDto {

  private Long id;
  private String email;
  private String firstName;
  private String lastName;
  private boolean isEmailVerified;
  private boolean isActive;
  private boolean isSuperuser;
  private Timestamp createdAt;
  private List<String> roles;
}
