package com.rumantra.shared;

import java.util.Map;

public class RumantraConstants {

  public static final String LOGIN_CONTAINER = "loginContainer";
  public static final String ARCH_ROLE = "ARCH_ROLES_RMTR";
  public static final String CLIENT_ROLE = "CLIENT_ROLES_RMTR";
  public static final String ADMIN_ROLE = "ADMIN_ROLES_RMTR";
  public static final String FREELANCE_CATEGORY_CODE = "FREELANCER";
  public static final String COMPANY_CATEGORY_CODE = "COMPANY";
  public static final String FREELANCE_CATEGORY = "Freelancer";
  public static final String COMPANY_CATEGORY = "Company";
  public static final Map<String, String> ARCHITECT_CATEGORY_MAP =
      Map.of(
          FREELANCE_CATEGORY_CODE, FREELANCE_CATEGORY,
          COMPANY_CATEGORY_CODE, COMPANY_CATEGORY);
}
