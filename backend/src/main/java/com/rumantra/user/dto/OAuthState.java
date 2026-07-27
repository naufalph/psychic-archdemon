package com.rumantra.user.dto;

import java.util.List;

import com.rumantra.legal.dto.AcceptanceRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthState {
  private String role;
  private List<AcceptanceRequest> acceptances;
}
