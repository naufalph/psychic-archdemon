package com.rumantra.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.rumantra.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

  private Long id;
  private String username;
  private String email;
  private String password;
  private Collection<? extends GrantedAuthority> authorities;

  public static UserPrincipal create(User user) {
    // For now, all architects have the same role
    Collection<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ARCHITECT"));

    return new UserPrincipal(
        user.getId(), user.getUserName(), user.getEmail(), user.getPassword(), authorities);
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
    return username;
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
