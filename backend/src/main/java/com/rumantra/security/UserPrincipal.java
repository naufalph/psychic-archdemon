package com.rumantra.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.rumantra.shared.RumantraConstants;
import com.rumantra.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

  private Long id;
  private String email;
  private String password;
  private Collection<? extends GrantedAuthority> authorities;

  public static UserPrincipal create(User user) {
    // Dynamically assign roles based on user's relationships to Architect and Client entities
    List<GrantedAuthority> authorities = new ArrayList<>();

    // Check if user is a superuser
    if (user.isSuperuser()) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + RumantraConstants.SUPERUSER_ROLE));
    }

    // Check if user has an Architect profile
    if (user.getArchitect() != null) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + RumantraConstants.ARCH_ROLE));
    }

    // Check if user has a Client profile
    if (user.getClient() != null) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + RumantraConstants.CLIENT_ROLE));
    }

    // Note: Users can authenticate with no roles initially.
    // They will activate roles via /api/users/me/activate-role endpoint.
    // Spring Security will still enforce role requirements on protected endpoints.

    return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), authorities);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
