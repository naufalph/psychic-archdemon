package com.rumantra.config;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
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

  @Value("${app.cors.allowed-origins:http://localhost:3000}")
  private String allowedOrigins;

  @Value("${app.security.hsts.enabled:false}")
  private boolean hstsEnabled;

  @Value("${app.security.hsts.max-age:31536000}")
  private long hstsMaxAge;

  @Value("${app.security.hsts.include-subdomains:true}")
  private boolean hstsIncludeSubdomains;

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

    // Support multiple origins separated by comma
    // Example: http://localhost:3000,https://yourdomain.com,https://www.yourdomain.com
    String[] origins = allowedOrigins.split(",");
    for (String origin : origins) {
      configuration.addAllowedOriginPattern(origin.trim());
    }

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
        // Security Headers Configuration
        .headers(
            headers ->
                headers
                    // Prevent clickjacking by disabling iframe embedding
                    .frameOptions(frame -> frame.deny())
                    // Prevent MIME sniffing
                    .contentTypeOptions(contentType -> contentType.disable())
                    // Enable XSS protection (legacy, but still useful for older browsers)
                    .xssProtection(
                        xss ->
                            xss.headerValue(
                                XXssProtectionHeaderWriter.HeaderValue.valueOf("1; mode=block")))
                    // Configure HSTS (only if enabled via environment variable)
                    .httpStrictTransportSecurity(
                        hsts -> {
                          if (hstsEnabled) {
                            hsts.maxAgeInSeconds(hstsMaxAge)
                                .includeSubDomains(hstsIncludeSubdomains);
                          } else {
                            hsts.disable();
                          }
                        })
                    // Add custom security headers
                    .addHeaderWriter(
                        (request, response) -> {
                          // Referrer Policy - only send origin when navigating cross-origin
                          response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

                          // Permissions Policy - control browser features
                          response.setHeader(
                              "Permissions-Policy", "camera=(), microphone=(), geolocation=(self)");

                          // Content Security Policy - prevent XSS and injection attacks
                          String csp =
                              "default-src 'self'; "
                                  + "script-src 'self'; "
                                  + "style-src 'self' 'unsafe-inline'; "
                                  + "img-src 'self' data: https:; "
                                  + "font-src 'self' data:; "
                                  + "connect-src 'self'; "
                                  + "frame-ancestors 'none'; "
                                  + "base-uri 'self'; "
                                  + "form-action 'self'";
                          response.setHeader("Content-Security-Policy", csp);
                        }))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Legacy architect endpoints
                    .requestMatchers("/api/v1/architects/signup", "/api/v1/architects/login")
                    .permitAll()

                    // New user endpoints (public)
                    .requestMatchers("/rmtr/users/login", "/rmtr/users/register")
                    .permitAll()
                    .requestMatchers("/rmtr/users/verify-email", "/rmtr/users/resend-verification")
                    .permitAll()
                    .requestMatchers("/rmtr/users/oauth2/**")
                    .permitAll()

                    // Protected endpoints
                    .requestMatchers("/api/v1/architects/profile", "/api/v1/architects/profile/**")
                    .authenticated()

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
