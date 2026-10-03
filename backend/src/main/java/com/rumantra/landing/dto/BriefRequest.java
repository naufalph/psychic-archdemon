package com.rumantra.landing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Submitted from the public landing mini-form, so every field is optional but size-capped — this
 * endpoint accepts unauthenticated writes.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BriefRequest {

  @Size(max = 60, message = "Preset slug must not exceed 60 characters")
  private String presetSlug;

  @Size(max = 40, message = "Building function must not exceed 40 characters")
  private String buildingFunction;

  @Size(max = 20, message = "Project scope must not exceed 20 characters")
  private String projectScope;

  @Size(max = 60, message = "Sub-category must not exceed 60 characters")
  private String subCategory;

  @Size(max = 160, message = "Title must not exceed 160 characters")
  private String title;

  @Size(max = 255, message = "Location must not exceed 255 characters")
  private String location;

  @Size(max = 255, message = "City must not exceed 255 characters")
  private String city;

  @Size(max = 100, message = "Province must not exceed 100 characters")
  private String province;

  @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
  @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
  private BigDecimal latitude;

  @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
  @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
  private BigDecimal longitude;

  @Min(value = 1, message = "Build area must be at least 1")
  @Max(value = 10000000, message = "Build area is unrealistically large")
  private Integer buildArea;

  @Min(value = 1, message = "Lot size must be at least 1")
  @Max(value = 10000000, message = "Lot size is unrealistically large")
  private Integer lotSize;

  @Min(value = 0, message = "Design budget must not be negative")
  private Long designBudgetTotal;

  @Min(value = 0, message = "Design budget must not be negative")
  private Long designBudgetMin;

  @Min(value = 0, message = "Design budget must not be negative")
  private Long designBudgetMax;

  @Size(max = 2000, message = "Description must not exceed 2000 characters")
  private String description;

  @Pattern(
      regexp = "^$|^\\+?[0-9\\s-]{10,16}$",
      message = "Invalid phone number format. Must be 10-16 digits.")
  private String phoneNumber;

  @Pattern(
      regexp = "^$|^(IMMEDIATELY|SPECIFIC_DATE)$",
      message = "Start date type must be IMMEDIATELY or SPECIFIC_DATE")
  private String startDateType;

  private LocalDate expectedStartDate;
}
