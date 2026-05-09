package com.rumantra.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DisbursementRequest {

  @NotBlank private String channelCode;

  @NotBlank private String accountNumber;

  @NotBlank private String accountHolderName;
}
