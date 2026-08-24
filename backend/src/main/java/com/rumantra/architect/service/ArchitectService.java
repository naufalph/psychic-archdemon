package com.rumantra.architect.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.dto.*;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.architect.repository.PortoRepository;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.shared.storage.FileStorageService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArchitectService {

  private final ArchitectRepository architectRepository;
  private final PortoRepository portoRepository;
  private final FileStorageService fileStorageService;

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

    if (updateRequest.getCompanySite() != null && !updateRequest.getCompanySite().isBlank()) {
      architect.setCompanySite(updateRequest.getCompanySite());
    }

    if (updateRequest.getContactName() != null) {
      architect.setContactName(updateRequest.getContactName());
    }

    if (updateRequest.getCategory() != null) {
      architect.setCategory(updateRequest.getCategory());
    }

    if (updateRequest.getPhoneNum() != null && !updateRequest.getPhoneNum().isBlank()) {
      architect.setPhoneNumber(updateRequest.getPhoneNum());
    }

    // Blank values are treated as "not provided yet" rather than an intentional clear,
    // since autosave re-sends the whole form on every debounced change.
    if (updateRequest.getKtpNum() != null
        && !updateRequest.getKtpNum().isBlank()
        && !updateRequest.getKtpNum().equals(architect.getKtpNum())) {
      // Check if new KTP number is already taken
      if (architectRepository.existsByKtpNum(updateRequest.getKtpNum())) {
        throw new IllegalArgumentException("KTP number is already registered!");
      }
      architect.setKtpNum(updateRequest.getKtpNum());
    }

    if (updateRequest.getNpwp() != null
        && !updateRequest.getNpwp().isBlank()
        && !updateRequest.getNpwp().equals(architect.getNpwp())) {
      // Check if new NPWP is already taken
      if (architectRepository.existsByNpwp(updateRequest.getNpwp())) {
        throw new IllegalArgumentException("NPWP is already registered!");
      }
      architect.setNpwp(updateRequest.getNpwp());
    }

    if (updateRequest.getFullnameKtp() != null) {
      architect.setFullnameKtp(updateRequest.getFullnameKtp());
    }

    if (updateRequest.getCity() != null) {
      architect.setCity(updateRequest.getCity());
    }

    if (updateRequest.getProvince() != null) {
      architect.setProvince(updateRequest.getProvince());
    }

    if (updateRequest.getFullAddress() != null) {
      architect.setFullAddress(updateRequest.getFullAddress());
    }

    if (updateRequest.getExperienceRange() != null) {
      architect.setExperienceRange(updateRequest.getExperienceRange());
    }

    if (updateRequest.getPhilosophy() != null) {
      architect.setPhilosophy(updateRequest.getPhilosophy());
    }

    if (updateRequest.getExpertise() != null) {
      architect.setExpertise(updateRequest.getExpertise());
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
  public ArchitectDto uploadPhoto(Long userId, MultipartFile file) {
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Architect not found for user ID: " + userId));

    String storedPath =
        fileStorageService.uploadImage(file, "architect/" + architect.getId() + "/profile");
    architect.setPhotoUrl(storedPath);
    architect = architectRepository.save(architect);

    return mapToDto(architect);
  }

  private boolean notBlank(String value) {
    return value != null && !value.trim().isEmpty();
  }

  private ProfileCompletionDto computeProfileCompletion(Architect architect) {
    boolean basicInfoComplete =
        notBlank(architect.getCompanyName())
            && notBlank(architect.getPhilosophy())
            && notBlank(architect.getExperienceRange())
            && architect.getExpertise() != null
            && !architect.getExpertise().isEmpty();

    boolean businessLocationComplete =
        notBlank(architect.getCategory())
            && notBlank(architect.getFullAddress())
            && notBlank(architect.getProvince())
            && notBlank(architect.getCity());

    boolean identityDocsComplete =
        notBlank(architect.getKtpNum())
            && notBlank(architect.getNpwp())
            && notBlank(architect.getFullnameKtp())
            && notBlank(architect.getPhoneNumber());

    boolean portfolioComplete = portoRepository.countByArchitectId(architect.getId()) > 0;

    int percent =
        (basicInfoComplete ? 25 : 0)
            + (businessLocationComplete ? 25 : 0)
            + (identityDocsComplete ? 25 : 0)
            + (portfolioComplete ? 25 : 0);

    return ProfileCompletionDto.builder()
        .basicInfoComplete(basicInfoComplete)
        .businessLocationComplete(businessLocationComplete)
        .identityDocsComplete(identityDocsComplete)
        .portfolioComplete(portfolioComplete)
        .percent(percent)
        .build();
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
        .fullAddress(architect.getFullAddress())
        .province(architect.getProvince())
        .photoUrl(
            architect.getPhotoUrl() != null
                ? fileStorageService.getPublicUrl(architect.getPhotoUrl())
                : null)
        .profileCompletion(computeProfileCompletion(architect))
        .build();
  }
}
