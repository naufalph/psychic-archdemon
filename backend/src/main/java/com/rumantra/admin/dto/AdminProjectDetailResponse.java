package com.rumantra.admin.dto;

import com.rumantra.client.dto.ProjectResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProjectDetailResponse {

  private ProjectResponse project;

  private String clientName;
  private String clientEmail;
  private String clientPhone;
  private boolean clientPhoneVerified;
  private String clientKtpNum;
  private boolean clientKtpVerified;
}
