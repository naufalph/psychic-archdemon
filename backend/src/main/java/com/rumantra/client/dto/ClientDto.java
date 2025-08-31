package com.rumantra.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {

  private Long id;
  private long userId;
  private String userName;
  private String email;
  private String phoneNumber;
  private String ktpNum;
  private boolean ktpVerified = false;
  private int projectMatch = 0;
  private int projectFinished = 0;
}
