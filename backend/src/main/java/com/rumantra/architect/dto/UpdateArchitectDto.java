package com.rumantra.architect.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArchitectDto {

  @Size(max = 255, message = "Company name must not exceed 255 characters")
  private String companyName;

  @Size(max = 255, message = "Company site must not exceed 255 characters")
  @Pattern(
      regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/.*)?$",
      message = "Company site must be a valid URL")
  private String companySite;

  @Size(max = 255, message = "Contact name must not exceed 255 characters")
  private String contactName;

  @Pattern(regexp = "^[0-9]{16}$", message = "KTP number must be exactly 16 digits")
  private String phoneNum;

  private String category;

  @Pattern(regexp = "^[0-9]{16}$", message = "KTP number must be exactly 16 digits")
  private String ktpNum;

  @Pattern(regexp = "^[0-9]{15,16}$", message = "NPWP must be 15 or 16 digits")
  private String npwp;
}
