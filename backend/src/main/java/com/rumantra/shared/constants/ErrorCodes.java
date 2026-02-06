package com.rumantra.shared.constants;

public class ErrorCodes {

  public static final String FIRST_NAME_REQUIRED = "firstName.required";
  public static final String LAST_NAME_REQUIRED = "lastName.required";
  public static final String NAME_VERIFIED_CANNOT_MODIFY = "name.verified.cannot.modify";

  public static final String PHONE_REQUIRED = "phone.required";
  public static final String PHONE_INVALID_FORMAT = "phone.invalid.format";

  public static final String KTP_REQUIRED = "ktp.required";
  public static final String KTP_INVALID_FORMAT = "ktp.invalid.format";
  public static final String KTP_VERIFIED_CANNOT_MODIFY = "ktp.verified.cannot.modify";

  private ErrorCodes() {}
}
