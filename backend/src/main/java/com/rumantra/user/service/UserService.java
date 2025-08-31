package com.rumantra.user.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import com.rumantra.user.dto.UserAuthResponseDto;
import com.rumantra.user.dto.UserDto;
import com.rumantra.user.dto.UserLoginRequestDto;
import com.rumantra.user.dto.UserSignupRequestDto;
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
}
