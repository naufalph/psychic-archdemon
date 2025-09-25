package com.rumantra.architect.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.dto.*;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.security.JwtUtils;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArchitectService {

  private final ArchitectRepository architectRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;

  @Transactional
  public ArchitectDto register(ArchitectSignupRequestDto signupRequest) {

    // Check if email already exists
    if (userRepository.existsByEmail(signupRequest.getEmail())) {
      throw new IllegalArgumentException("Email is already in use!");
    }

    // Check if KTP number already exists
    if (architectRepository.existsByKtpNum(signupRequest.getKtpNum())) {
      throw new IllegalArgumentException("KTP number is already registered!");
    }

    // Check if NPWP already exists
    if (architectRepository.existsByNpwp(signupRequest.getNpwp())) {
      throw new IllegalArgumentException("NPWP is already registered!");
    }

    // Create new user
    Optional<User> user = userRepository.findByEmailAndIsActive(signupRequest.getEmail(), true);

    if (user.isEmpty()) {
      throw new IllegalArgumentException("user is not found!");
    } else {
      // Create architect profile
      Architect architect =
          Architect.builder()
              .user(user.get())
              .companyName(signupRequest.getCompanyName())
              .companySite(signupRequest.getCompanySite())
              .contactName(signupRequest.getContactName())
              .phoneNumber(signupRequest.getPhoneNum())
              .category(signupRequest.getCategory())
              .ktpNum(signupRequest.getKtpNum())
              .ktpVerified(false)
              .npwp(signupRequest.getNpwp())
              .npwpVerified(false)
              .bidLeft(10)
              .successMatch(0)
              .successProject(0)
              .build();

      architect = architectRepository.save(architect);

      return mapToDto(architect);
    }
  }

  @Transactional
  public ArchitectDto updateArchitect(Long userId, UpdateArchitectDto updateRequest) {
    // Find architect by user ID
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Architect not found for user ID: " + userId));

    User user = architect.getUser();

    // Update user fields if provided
    if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
      // Check if new email is already taken
      if (userRepository.existsByEmail(updateRequest.getEmail())) {
        throw new IllegalArgumentException("Email is already in use!");
      }
      user.setEmail(updateRequest.getEmail());
    }

    if (updateRequest.getPassword() != null) {
      user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
    }

    user.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
    userRepository.save(user);

    // Update architect fields if provided
    if (updateRequest.getCompanyName() != null) {
      architect.setCompanyName(updateRequest.getCompanyName());
    }

    if (updateRequest.getCompanySite() != null) {
      architect.setCompanySite(updateRequest.getCompanySite());
    }

    if (updateRequest.getContactName() != null) {
      architect.setContactName(updateRequest.getContactName());
    }

    if (updateRequest.getCategory() != null) {
      architect.setCategory(updateRequest.getCategory());
    }

    if (updateRequest.getPhoneNum() != null) {
      architect.setPhoneNumber(updateRequest.getPhoneNum());
    }

    if (updateRequest.getKtpNum() != null
        && !updateRequest.getKtpNum().equals(architect.getKtpNum())) {
      // Check if new KTP number is already taken
      if (architectRepository.existsByKtpNum(updateRequest.getKtpNum())) {
        throw new IllegalArgumentException("KTP number is already registered!");
      }
      architect.setKtpNum(updateRequest.getKtpNum());
    }

    if (updateRequest.getNpwp() != null && !updateRequest.getNpwp().equals(architect.getNpwp())) {
      // Check if new NPWP is already taken
      if (architectRepository.existsByNpwp(updateRequest.getNpwp())) {
        throw new IllegalArgumentException("NPWP is already registered!");
      }
      architect.setNpwp(updateRequest.getNpwp());
    }

    architect = architectRepository.save(architect);

    return mapToDto(architect);
  }

  public ArchitectDto getArchitectByUserId(Long userId) {
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Architect not found for user ID: " + userId));
    return mapToDto(architect);
  }

  private ArchitectDto mapToDto(Architect architect) {
    return ArchitectDto.builder()
        .id(architect.getId())
        .userId(architect.getUser().getId())
        .email(architect.getUser().getEmail())
        .category(architect.getCategory())
        .phoneNumber(architect.getPhoneNumber())
        .companyName(architect.getCompanyName())
        .companySite(architect.getCompanySite())
        .contactName(architect.getContactName())
        .ktpNum(architect.getKtpNum())
        .ktpVerified(architect.isKtpVerified())
        .npwp(architect.getNpwp())
        .npwpVerified(architect.isNpwpVerified())
        .bidLeft(architect.getBidLeft())
        .successMatch(architect.getSuccessMatch())
        .successProject(architect.getSuccessProject())
        .build();
  }
}
