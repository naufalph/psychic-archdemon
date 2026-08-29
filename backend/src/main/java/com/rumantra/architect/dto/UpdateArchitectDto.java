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
      regexp = "^$|^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/.*)?$",
      message = "Company site must be a valid URL")
  private String companySite;

  @Size(max = 255, message = "Contact name must not exceed 255 characters")
  private String contactName;

  @Pattern(regexp = "^$|^[0-9]{8,16}$", message = "Phone number must be 8 to 16 digits")
  private String phoneNum;

  private String category;

  @Pattern(regexp = "^$|^[0-9]{16}$", message = "KTP number must be exactly 16 digits")
  private String ktpNum;

  @Pattern(regexp = "^$|^[0-9]{15,16}$", message = "NPWP must be 15 or 16 digits")
  private String npwp;

  @Size(max = 255, message = "Full name must not exceed 255 characters")
  private String fullnameKtp;

  @Size(max = 255, message = "City must not exceed 255 characters")
  private String city;

  @Size(max = 100, message = "Province must not exceed 100 characters")
  private String province;

  private String fullAddress;

  private String experienceRange;

  private String philosophy;

  private java.util.List<String> expertise;

  private java.util.List<EducationEntry> education;
}
