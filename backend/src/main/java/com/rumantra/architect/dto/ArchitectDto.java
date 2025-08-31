package com.rumantra.architect.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchitectDto {

  private Long id;
  private Long userId;
  private String email;
  private String companyName;
  private String companySite;
  private String category;
  private String phoneNumber;
  private String contactName;
  private String ktpNum;
  private boolean ktpVerified;
  private String npwp;
  private boolean npwpVerified;
  private int bidLeft;
  private int successMatch;
  private int successProject;
}
