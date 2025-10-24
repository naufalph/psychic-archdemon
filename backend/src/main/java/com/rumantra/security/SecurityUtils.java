package com.rumantra.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for retrieving information about the currently authenticated user from the security
 * context.
 */
public class SecurityUtils {

  private SecurityUtils() {
    // Private constructor to prevent instantiation
  }

  /**
   * Get the current authenticated user's ID.
   *
   * @return The user ID
   * @throws IllegalStateException if no user is authenticated
   */
  public static Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found");
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof UserPrincipal) {
      return ((UserPrincipal) principal).getId();
    }

    throw new IllegalStateException(
        "Authentication principal is not of type UserPrincipal: " + principal.getClass().getName());
  }

  /**
   * Get the current authenticated user's email.
   *
   * @return The user email
   * @throws IllegalStateException if no user is authenticated
   */
  public static String getCurrentUserEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found");
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof UserPrincipal) {
      return ((UserPrincipal) principal).getEmail();
    }

    throw new IllegalStateException(
        "Authentication principal is not of type UserPrincipal: " + principal.getClass().getName());
  }

  /**
   * Get the current UserPrincipal.
   *
   * @return The UserPrincipal
   * @throws IllegalStateException if no user is authenticated
   */
  public static UserPrincipal getCurrentUserPrincipal() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("No authenticated user found");
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof UserPrincipal) {
      return (UserPrincipal) principal;
    }

    throw new IllegalStateException(
        "Authentication principal is not of type UserPrincipal: " + principal.getClass().getName());
  }

  /**
   * Check if the current user has a specific role.
   *
   * @param role The role to check (without "ROLE_" prefix, e.g., "ARCHITECT", "CLIENT")
   * @return true if user has the role, false otherwise
   */
  public static boolean hasRole(String role) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    return authentication.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
  }
}
