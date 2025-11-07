package com.rumantra.shared;

import java.util.Map;

public class RumantraConstants {

  public static final String LOGIN_CONTAINER = "loginContainer";

  // Role constants - these are used in both API requests and Spring Security
  // Spring Security will automatically prefix with "ROLE_" internally
  public static final String ARCH_ROLE = "ARCHITECT";
  public static final String CLIENT_ROLE = "CLIENT";
  public static final String ADMIN_ROLE = "ADMIN";
  public static final String SUPERUSER_ROLE = "SUPERUSER";
  public static final String FREELANCE_CATEGORY_CODE = "FREELANCER";
  public static final String COMPANY_CATEGORY_CODE = "COMPANY";
  public static final String FREELANCE_CATEGORY = "Freelancer";
  public static final String COMPANY_CATEGORY = "Company";
  public static final Map<String, String> ARCHITECT_CATEGORY_MAP =
      Map.of(
          FREELANCE_CATEGORY_CODE, FREELANCE_CATEGORY,
          COMPANY_CATEGORY_CODE, COMPANY_CATEGORY);
}
