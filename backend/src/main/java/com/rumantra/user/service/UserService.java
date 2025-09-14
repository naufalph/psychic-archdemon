package com.rumantra.user.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
import com.rumantra.user.domain.User;
import com.rumantra.user.dto.*;
import com.rumantra.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final ArchitectService architectService;
  private final ClientService clientService;
  private final ArchitectRepository architectRepository;
  private final ClientRepository clientRepository;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String googleClientId;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String googleClientSecret;

  @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
  private String redirectUri;

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public UserAuthResponseDto login(UserLoginRequestDto loginRequest) {
    User user =
        userRepository
            .findByEmailAndIsActive(loginRequest.getEmail())
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

  public UserDto getUserByUserId(Long userId) {
    User user =
        userRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("User not found for user ID: " + userId));
    return mapToDto(user);
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

  public UserDto register(UserSignupRequestDto signupRequest) {
    // Check if email already exists
    if (userRepository.existsByEmail(signupRequest.getEmail())) {
      throw new IllegalArgumentException("Email is already in use!");
    }

    // Create new user
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

    // Create client profile
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
              .findByEmail(googleUser.getEmail())
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
            .firstName(firstName)
            .lastName(lastName)
            .isEmailVerified(true)
            .isActive(true)
            .createdAt(Timestamp.valueOf(LocalDateTime.now()))
            .build();

    return userRepository.save(newUser);
  }
}
