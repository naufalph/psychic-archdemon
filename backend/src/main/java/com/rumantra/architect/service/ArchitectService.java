package com.rumantra.architect.service;

import org.springframework.stereotype.Service;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.dto.*;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.shared.exception.ResourceNotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArchitectService {

  private final ArchitectRepository architectRepository;

  @Transactional
  public ArchitectDto updateArchitect(Long userId, UpdateArchitectDto updateRequest) {
    // Find architect by user ID
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Architect not found for user ID: " + userId));

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

    if (updateRequest.getFullnameKtp() != null) {
      architect.setFullnameKtp(updateRequest.getFullnameKtp());
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

  @Transactional
  public ArchitectDto updateProfile(Long userId, UpdateArchitectProfileRequest request) {
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Architect not found for user ID: " + userId));

    if (request.getCompanyName() != null) {
      architect.setCompanyName(request.getCompanyName());
    }

    if (request.getCity() != null) {
      architect.setCity(request.getCity());
    }

    if (request.getExperienceRange() != null) {
      architect.setExperienceRange(request.getExperienceRange());
    }

    if (request.getPhilosophy() != null) {
      architect.setPhilosophy(request.getPhilosophy());
    }

    if (request.getExpertise() != null) {
      architect.setExpertise(request.getExpertise());
    }

    architect.setNeedsOnboarding(false);
    architect.setOnboardingCompletedAt(new java.sql.Timestamp(System.currentTimeMillis()));

    architect = architectRepository.save(architect);

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
        .fullnameKtp(architect.getFullnameKtp())
        .phoneVerified(architect.isPhoneVerified())
        .successMatch(architect.getSuccessMatch())
        .successProject(architect.getSuccessProject())
        .city(architect.getCity())
        .experienceRange(architect.getExperienceRange())
        .philosophy(architect.getPhilosophy())
        .expertise(architect.getExpertise())
        .needsOnboarding(architect.getNeedsOnboarding())
        .onboardingCompletedAt(architect.getOnboardingCompletedAt())
        .build();
  }
}
