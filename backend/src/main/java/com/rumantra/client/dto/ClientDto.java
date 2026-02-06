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
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private String ktpNum;
  @Builder.Default private boolean ktpVerified = false;
  @Builder.Default private int projectMatch = 0;
  @Builder.Default private int projectFinished = 0;
}
