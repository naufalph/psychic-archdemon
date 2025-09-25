package com.rumantra.architect.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchitectAuthResponseDto {

  private String token;
  private String type;
  private Long id;
  private String userName;
  private String email;
  private ArchitectDto architect;
}
