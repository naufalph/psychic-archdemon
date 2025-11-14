package com.rumantra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.rumantra.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final UserDetailsService userDetailsService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final PasswordEncoder passwordEncoder;

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOriginPattern("http://localhost:3000");
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // New user endpoints (public)
                    .requestMatchers("/rmtr/users/login", "/rmtr/users/register")
                    .permitAll()
                    .requestMatchers("/rmtr/users/verify-email", "/rmtr/users/resend-verification")
                    .permitAll()
                    .requestMatchers("/rmtr/users/oauth2/**")
                    .permitAll()

                    // User profile endpoints (authenticated)
                    .requestMatchers("/rmtr/users/me/**")
                    .authenticated()

                    // Notification endpoints (authenticated)
                    .requestMatchers("/api/notifications/**")
                    .authenticated()

                    // Protected endpoints
                    .requestMatchers("/api/v1/architects/profile", "/api/v1/architects/profile/**")
                    .authenticated()

                    // Porto endpoints - require ARCHITECT role
                    .requestMatchers("/api/architects/*/portos/**")
                    .hasRole("ARCHITECT")
                    .requestMatchers("/api/architects/*/portos")
                    .hasRole("ARCHITECT")
                    .requestMatchers("/api/portos/**")
                    .hasRole("ARCHITECT")

                    // Client project endpoints - require CLIENT role
                    .requestMatchers("/api/clients/*/projects/**")
                    .hasRole("CLIENT")
                    .requestMatchers("/api/clients/*/projects")
                    .hasRole("CLIENT")
                    .requestMatchers("/api/v1/projects/{projectId}/validate")
                    .hasRole("SUPERUSER")
                    .requestMatchers("/api/v1/projects/all")
                    .hasRole("SUPERUSER")
                    .requestMatchers("/api/projects/**")
                    .hasRole("CLIENT")

                    // Static file serving (uploads)
                    .requestMatchers("/uploads/**")
                    .permitAll()

                    // Documentation
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
