package com.rumantra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

  @Value("${app.frontend.url:http://localhost:3001}")
  private String frontendUrl;

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
    configuration.addAllowedOriginPattern("http://localhost:3001");
    configuration.addAllowedOriginPattern("http://*:3001");
    configuration.addAllowedOriginPattern("http://*:8080");
    configuration.addAllowedOriginPattern("http://*.ts.net:3000");
    configuration.addAllowedOriginPattern("http://*.ts.net:3001");
    configuration.addAllowedOriginPattern("http://*.ts.net:8080");
    configuration.addAllowedOriginPattern(frontendUrl);
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
        .csrf(AbstractHttpConfigurer::disable)
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
                    .requestMatchers("/rmtr/notifications/**")
                    .authenticated()

                    // WebSocket endpoints (public for handshake, auth in WebSocketConfig)
                    .requestMatchers("/ws/**")
                    .permitAll()

                    // Chat endpoints (authenticated)
                    .requestMatchers("/rmtr/chat/**")
                    .authenticated()

                    // Admin backoffice endpoints — SUPERUSER only
                    .requestMatchers("/rmtr/admin/**")
                    .hasRole("SUPERUSER")

                    // Support conversation endpoints
                    .requestMatchers(HttpMethod.POST, "/rmtr/support/conversations")
                    .hasAnyRole("ARCHITECT", "CLIENT")
                    .requestMatchers(HttpMethod.GET, "/rmtr/support/conversations")
                    .hasRole("SUPERUSER")

                    // Architect profile & OTP endpoints - require ARCHITECT role
                    .requestMatchers("/rmtr/architects/**")
                    .hasRole("ARCHITECT")

                    // Porto endpoints - require ARCHITECT role
                    .requestMatchers("/rmtr/porto/**")
                    .hasRole("ARCHITECT")

                    // Bid endpoints — CLIENT-specific actions first
                    .requestMatchers(HttpMethod.POST, "/rmtr/bids/*/accept")
                    .hasRole("CLIENT")
                    .requestMatchers(HttpMethod.GET, "/rmtr/bids/*")
                    .authenticated()
                    // All other bid operations — ARCHITECT only
                    .requestMatchers("/rmtr/bids/**")
                    .hasRole("ARCHITECT")

                    // Subscription endpoints - require ARCHITECT role (except webhook)
                    .requestMatchers("/rmtr/subscriptions/webhook")
                    .permitAll()
                    .requestMatchers("/rmtr/subscriptions/**")
                    .hasRole("ARCHITECT")

                    // Token purchase endpoints - require ARCHITECT role
                    .requestMatchers("/rmtr/tokens/**")
                    .hasRole("ARCHITECT")

                    // Unified Xendit webhook endpoints (public, token-verified)
                    .requestMatchers("/rmtr/xendit/webhook/**")
                    .permitAll()

                    // Phase payment endpoints - require CLIENT role
                    .requestMatchers(HttpMethod.GET, "/rmtr/payments/projects/**")
                    .hasRole("CLIENT")
                    .requestMatchers(HttpMethod.POST, "/rmtr/payments/phases/**")
                    .hasRole("CLIENT")

                    // Client project endpoints - require CLIENT role
                    .requestMatchers("/rmtr/clients/*/projects/**")
                    .hasRole("CLIENT")
                    .requestMatchers("/rmtr/clients/*/projects")
                    .hasRole("CLIENT")
                    .requestMatchers("/rmtr/projects/*/confirm-negotiation")
                    .hasRole("CLIENT")
                    .requestMatchers("/rmtr/projects/*/reject-negotiation")
                    .hasRole("CLIENT")
                    .requestMatchers("/rmtr/projects/*/architect-confirm-negotiation")
                    .hasRole("ARCHITECT")
                    .requestMatchers("/rmtr/projects/{projectId}/validate")
                    .hasRole("SUPERUSER")
                    .requestMatchers("/rmtr/projects/all")
                    .hasRole("SUPERUSER")
                    .requestMatchers("/rmtr/projects/*/for-architect")
                    .hasRole("ARCHITECT")
                    .requestMatchers("/rmtr/projects/open")
                    .hasRole("ARCHITECT")
                    .requestMatchers(HttpMethod.GET, "/rmtr/projects/public-preview")
                    .permitAll()

                    // Phase lifecycle endpoints — must be declared before the broad
                    // "/rmtr/projects/**" CLIENT-only catch-all below, otherwise that
                    // catch-all matches first and architects get 403 on these (which
                    // are documented as accessible to both parties).
                    .requestMatchers(HttpMethod.POST, "/rmtr/projects/*/phases")
                    .hasRole("CLIENT")
                    .requestMatchers(HttpMethod.GET, "/rmtr/projects/*/phases/**")
                    .authenticated()
                    .requestMatchers("/rmtr/projects/**")
                    .hasRole("CLIENT")
                    .requestMatchers(HttpMethod.POST, "/rmtr/phases/*/bill")
                    .hasRole("CLIENT")
                    .requestMatchers(HttpMethod.POST, "/rmtr/phases/*/approve")
                    .hasRole("CLIENT")
                    .requestMatchers(HttpMethod.POST, "/rmtr/phases/*/dispute")
                    .hasRole("CLIENT")
                    .requestMatchers(HttpMethod.POST, "/rmtr/phases/*/deliverables")
                    .hasRole("ARCHITECT")
                    .requestMatchers(HttpMethod.POST, "/rmtr/phases/*/disburse")
                    .hasRole("ARCHITECT")
                    .requestMatchers(HttpMethod.GET, "/rmtr/phases/*/logs")
                    .authenticated()

                    // Static file serving (uploads)
                    .requestMatchers("/uploads/**")
                    .permitAll()

                    // Health check (for Railway deployment)
                    .requestMatchers("/actuator/health")
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
