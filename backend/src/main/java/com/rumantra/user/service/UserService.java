package com.rumantra.user.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.architect.service.ArchitectService;
import com.rumantra.client.domain.Client;
import com.rumantra.client.dto.ClientSignupRequestDto;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.client.service.ClientService;
import com.rumantra.security.JwtUtils;
import com.rumantra.shared.RumantraConstants;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.user.domain.EmailVerification;
import com.rumantra.user.domain.SocialType;
import com.rumantra.user.domain.User;
import com.rumantra.user.dto.*;
import com.rumantra.user.dto.LinkedInUserInfoDto;
import com.rumantra.user.repository.EmailVerificationRepository;
import com.rumantra.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final EmailVerificationRepository emailVerificationRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final ArchitectService architectService;
  private final ClientService clientService;
  private final ArchitectRepository architectRepository;
  private final ClientRepository clientRepository;
  private final EmailService emailService;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String googleClientId;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String googleClientSecret;

  @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
  private String redirectUri;

  @Value("${spring.security.oauth2.client.registration.linkedin.client-id}")
  private String linkedinClientId;

  @Value("${spring.security.oauth2.client.registration.linkedin.client-secret}")
  private String linkedinClientSecret;

  @Value("${spring.security.oauth2.client.registration.linkedin.redirect-uri}")
  private String linkedinRedirectUri;

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public UserAuthResponseDto login(UserLoginRequestDto loginRequest) {
    User user =
        userRepository
            .findByEmailAndSocialTypeAndIsActive(loginRequest.getEmail(), SocialType.EMAIL, true)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "User not found with username or email: " + loginRequest.getEmail()));

    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      throw new IllegalArgumentException("Invalid password!");
    }

    if (!user.isActive()) {
      throw new IllegalArgumentException("User account is deactivated!");
    }

    if (!user.isEmailVerified()) {
      throw new IllegalArgumentException("Please verify your email before logging in!");
    }

    List<String> registeredRoles = new ArrayList<>();
    autoRegisterWhenSignIn(user, loginRequest, registeredRoles);

    String jwt = jwtUtils.generateJwtToken(user.getEmail());

    return UserAuthResponseDto.builder()
        .token(jwt)
        .type("Bearer")
        .id(user.getId())
        .email(user.getEmail())
        .registeredRoles(registeredRoles)
        .build();
  }

  private void autoRegisterWhenSignIn(
      User user, UserLoginRequestDto loginRequest, List<String> registeredRoles) {
    if (RumantraConstants.ARCH_ROLE.equals(loginRequest.getRole())) {
      try {
        architectService.getArchitectByUserId(user.getId());
      } catch (ResourceNotFoundException e) {
        // This is expected when user is not an architect
        Architect architect =
            Architect.builder()
                .user(user)
                .ktpVerified(false)
                .npwpVerified(false)
                .bidLeft(10)
                .successMatch(0)
                .successProject(0)
                .build();
        architectRepository.save(
            architect); // create architect instance whenever user login with blank profile
      }
      registeredRoles.add(RumantraConstants.ARCH_ROLE);
    } else if (RumantraConstants.CLIENT_ROLE.equals(loginRequest.getRole())) {
      try {
        clientService.getClientByUserId(user.getId());
      } catch (ResourceNotFoundException e) {
        // This is expected when user is not a client
        Client client =
            Client.builder()
                .user(user)
                .ktpVerified(false)
                .projectMatch(0)
                .projectFinished(0)
                .build();
        clientRepository.save(
            client); // create user instance whenever user login with blank profile
      }
      registeredRoles.add(RumantraConstants.CLIENT_ROLE);
    }
  }

  private UserDto mapToDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .isEmailVerified(user.isEmailVerified())
        .isActive(user.isActive())
        .build();
  }

  @Transactional
  public UserDto register(UserSignupRequestDto signupRequest) {
    // Check if email already exists for EMAIL social type
    if (userRepository.existsByEmailAndSocialType(signupRequest.getEmail(), SocialType.EMAIL)) {
      throw new IllegalArgumentException("Email is already in use!");
    }

    // Create new user with email unverified
    User user =
        User.builder()
            .email(signupRequest.getEmail())
            .password(passwordEncoder.encode(signupRequest.getPassword()))
            .firstName(signupRequest.getFirstName())
            .lastName(signupRequest.getLastName())
            .isEmailVerified(false)
            .isActive(true)
            .createdAt(Timestamp.valueOf(LocalDateTime.now()))
            .build();

    user = userRepository.save(user);

    // Generate and send verification email
    String verificationToken = generateVerificationToken();
    saveVerificationToken(user.getId(), verificationToken);
    emailService.sendVerificationEmail(user.getEmail(), verificationToken);

    // Create client profile (but user can't log in until verified)
    ClientSignupRequestDto clientSignupRequestDto =
        ClientSignupRequestDto.builder().userId(user.getId()).build();

    clientService.register(clientSignupRequestDto);

    return mapToDto(user);
  }

  public String getGoogleAuthorizationUrl() {
    String baseUrl = "https://accounts.google.com/o/oauth2/v2/auth";
    String scope = "email profile";

    return String.format(
        "%s?client_id=%s&response_type=code&scope=%s&redirect_uri=%s&access_type=offline",
        baseUrl, googleClientId, scope, redirectUri);
  }

  public UserAuthResponseDto processGoogleCallback(String code) {
    try {
      // 1. Exchange code for tokens
      String tokenUrl = "https://oauth2.googleapis.com/token";
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

      String requestBody =
          String.format(
              "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
              code, googleClientId, googleClientSecret, redirectUri);

      ResponseEntity<String> tokenResponse =
          restTemplate.exchange(
              tokenUrl, HttpMethod.POST, new HttpEntity<>(requestBody, headers), String.class);

      JsonNode tokenNode = objectMapper.readTree(tokenResponse.getBody());
      String accessToken = tokenNode.get("access_token").asText();

      // 2. Get user info using access token
      HttpHeaders userInfoHeaders = new HttpHeaders();
      userInfoHeaders.setBearerAuth(accessToken);

      ResponseEntity<String> userInfoResponse =
          restTemplate.exchange(
              "https://www.googleapis.com/oauth2/v3/userinfo",
              HttpMethod.GET,
              new HttpEntity<>(userInfoHeaders),
              String.class);

      JsonNode userInfo = objectMapper.readTree(userInfoResponse.getBody());

      GoogleUserInfoDto googleUser =
          GoogleUserInfoDto.builder()
              .email(userInfo.get("email").asText())
              .name(userInfo.get("name").asText())
              .picture(userInfo.get("picture").asText())
              .emailVerified(userInfo.get("email_verified").asBoolean())
              .build();

      User user =
          userRepository
              .findByEmailAndSocialType(googleUser.getEmail(), SocialType.GOOGLE)
              .orElseGet(() -> createGoogleUser(googleUser));

      String jwt = jwtUtils.generateJwtToken(user.getEmail());
      List<String> registeredRoles = new ArrayList<>();

      // Add default client role for Google users
      if (clientRepository.findByUserId(user.getId()).isEmpty()) {
        Client client =
            Client.builder()
                .user(user)
                .ktpVerified(false)
                .projectMatch(0)
                .projectFinished(0)
                .build();
        clientRepository.save(client);
      }
      registeredRoles.add(RumantraConstants.CLIENT_ROLE);

      return UserAuthResponseDto.builder()
          .token(jwt)
          .type("Bearer")
          .id(user.getId())
          .email(user.getEmail())
          .registeredRoles(registeredRoles)
          .build();

    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to process Google login: " + e.getMessage());
    }
  }

  private User createGoogleUser(GoogleUserInfoDto googleUser) {
    if (!googleUser.isEmailVerified()) {
      throw new IllegalArgumentException("Google email is not verified!");
    }

    // Split name into first and last name
    String[] nameParts = googleUser.getName().split(" ", 2);
    String firstName = nameParts[0];
    String lastName = nameParts.length > 1 ? nameParts[1] : "";

    User newUser =
        User.builder()
            .email(googleUser.getEmail())
            .socialType(SocialType.GOOGLE)
            .firstName(firstName)
            .lastName(lastName)
            .isEmailVerified(true)
            .isActive(true)
            .createdAt(Timestamp.valueOf(LocalDateTime.now()))
            .build();

    return userRepository.save(newUser);
  }

  public String getLinkedInAuthorizationUrl() {
    String baseUrl = "https://www.linkedin.com/oauth/v2/authorization";
    String scope = "openid profile email";

    return String.format(
        "%s?response_type=code&client_id=%s&redirect_uri=%s&scope=%s",
        baseUrl, linkedinClientId, linkedinRedirectUri, scope);
  }

  public UserAuthResponseDto processLinkedInCallback(String code) {
    try {
      // 1. Exchange code for access token
      String tokenUrl = "https://www.linkedin.com/oauth/v2/accessToken";
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

      // URL encode parameters to handle special characters
      String encodedCode = java.net.URLEncoder.encode(code, "UTF-8");
      String encodedRedirectUri = java.net.URLEncoder.encode(linkedinRedirectUri, "UTF-8");
      String encodedClientId = java.net.URLEncoder.encode(linkedinClientId, "UTF-8");
      String encodedClientSecret = java.net.URLEncoder.encode(linkedinClientSecret, "UTF-8");

      String requestBody =
          String.format(
              "grant_type=authorization_code&code=%s&redirect_uri=%s&client_id=%s&client_secret=%s",
              encodedCode, encodedRedirectUri, encodedClientId, encodedClientSecret);

      ResponseEntity<String> tokenResponse;
      try {
        tokenResponse =
            restTemplate.exchange(
                tokenUrl, HttpMethod.POST, new HttpEntity<>(requestBody, headers), String.class);
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "LinkedIn token exchange request failed: " + e.getMessage());
      }

      if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
        throw new IllegalArgumentException(
            "LinkedIn token exchange failed with status: "
                + tokenResponse.getStatusCode()
                + ", body: "
                + tokenResponse.getBody());
      }

      JsonNode tokenNode = objectMapper.readTree(tokenResponse.getBody());
      if (!tokenNode.has("access_token")) {
        throw new IllegalArgumentException(
            "No access token in LinkedIn response: " + tokenResponse.getBody());
      }
      String accessToken = tokenNode.get("access_token").asText();

      // 2. Get user profile info using OpenID Connect userinfo endpoint
      HttpHeaders profileHeaders = new HttpHeaders();
      profileHeaders.setBearerAuth(accessToken);

      ResponseEntity<String> profileResponse =
          restTemplate.exchange(
              "https://api.linkedin.com/v2/userinfo",
              HttpMethod.GET,
              new HttpEntity<>(profileHeaders),
              String.class);

      JsonNode profileInfo = objectMapper.readTree(profileResponse.getBody());
      String email = profileInfo.get("email").asText();

      // 4. Extract profile picture from userinfo response
      String profilePicture = "";
      if (profileInfo.has("picture")) {
        profilePicture = profileInfo.get("picture").asText();
      }

      // 5. Parse name from userinfo response
      String firstName = "";
      String lastName = "";
      if (profileInfo.has("given_name")) {
        firstName = profileInfo.get("given_name").asText();
      }
      if (profileInfo.has("family_name")) {
        lastName = profileInfo.get("family_name").asText();
      }

      // Fallback to name field if given_name/family_name not available
      if (firstName.isEmpty() && profileInfo.has("name")) {
        String fullName = profileInfo.get("name").asText();
        String[] nameParts = fullName.split(" ", 2);
        firstName = nameParts[0];
        lastName = nameParts.length > 1 ? nameParts[1] : "";
      }

      LinkedInUserInfoDto linkedinUser =
          LinkedInUserInfoDto.builder()
              .id(profileInfo.get("sub").asText()) // 'sub' is the user ID in OpenID Connect
              .firstName(firstName)
              .lastName(lastName)
              .email(email)
              .profilePicture(profilePicture)
              .emailVerified(true) // LinkedIn emails are considered verified
              .build();

      User user =
          userRepository
              .findByEmailAndSocialType(linkedinUser.getEmail(), SocialType.LINKEDIN)
              .orElseGet(() -> createLinkedInUser(linkedinUser));

      String jwt = jwtUtils.generateJwtToken(user.getEmail());
      List<String> registeredRoles = new ArrayList<>();

      // Add default client role for LinkedIn users
      if (clientRepository.findByUserId(user.getId()).isEmpty()) {
        Client client =
            Client.builder()
                .user(user)
                .ktpVerified(false)
                .projectMatch(0)
                .projectFinished(0)
                .build();
        clientRepository.save(client);
      }
      registeredRoles.add(RumantraConstants.CLIENT_ROLE);

      return UserAuthResponseDto.builder()
          .token(jwt)
          .type("Bearer")
          .id(user.getId())
          .email(user.getEmail())
          .registeredRoles(registeredRoles)
          .build();

    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to process LinkedIn login: " + e.getMessage());
    }
  }

  private User createLinkedInUser(LinkedInUserInfoDto linkedinUser) {
    if (!linkedinUser.isEmailVerified()) {
      throw new IllegalArgumentException("LinkedIn email is not verified!");
    }

    User newUser =
        User.builder()
            .email(linkedinUser.getEmail())
            .socialType(SocialType.LINKEDIN)
            .firstName(linkedinUser.getFirstName())
            .lastName(linkedinUser.getLastName())
            .isEmailVerified(true)
            .isActive(true)
            .createdAt(Timestamp.valueOf(LocalDateTime.now()))
            .build();

    return userRepository.save(newUser);
  }

  public void verifyEmail(String token) {
    EmailVerification verification =
        emailVerificationRepository
            .findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

    if (verification.isExpired()) {
      throw new IllegalArgumentException("Verification token has expired");
    }

    if (verification.isVerified()) {
      throw new IllegalArgumentException("Email has already been verified");
    }

    // Mark user as verified
    User user =
        userRepository
            .findById(verification.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    user.setEmailVerified(true);
    userRepository.save(user);

    // Mark token as used
    verification.setVerified(true);
    emailVerificationRepository.save(verification);

    // Send welcome email
    emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());
  }

  public void resendVerificationEmail(String email) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found with email: " + email));

    if (user.isEmailVerified()) {
      throw new IllegalArgumentException("Email is already verified");
    }

    // Invalidate existing tokens for this user
    emailVerificationRepository.deleteByUserId(user.getId());

    // Generate new token and send email
    String verificationToken = generateVerificationToken();
    saveVerificationToken(user.getId(), verificationToken);
    emailService.sendVerificationEmail(user.getEmail(), verificationToken);
  }

  private String generateVerificationToken() {
    return UUID.randomUUID().toString();
  }

  private void saveVerificationToken(Long userId, String token) {
    EmailVerification verification =
        EmailVerification.builder()
            .userId(userId)
            .token(token)
            .expiry(Timestamp.valueOf(LocalDateTime.now().plusHours(24)))
            .verified(false)
            .build();

    emailVerificationRepository.save(verification);
  }
}
