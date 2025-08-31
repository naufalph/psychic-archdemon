package com.rumantra.client.service;

import org.springframework.stereotype.Service;

import com.rumantra.client.domain.Client;
import com.rumantra.client.dto.ClientDto;
import com.rumantra.client.dto.ClientSignupRequestDto;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.user.domain.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {

  private final ClientRepository clientRepository;

  @Transactional
  public ClientDto register(ClientSignupRequestDto signupRequest) {

    User user = User.builder().id(signupRequest.getUserId()).build();

    Client client =
        Client.builder()
            .user(user)
            .phoneNumber(signupRequest.getPhoneNumber())
            .ktpNum(signupRequest.getKtpNum())
            .ktpVerified(false)
            .projectMatch(0)
            .projectFinished(0)
            .build();

    client = clientRepository.save(client);

    return mapToDto(client);
  }

  //    @Transactional
  //    public ArchitectDto updateArchitect(Long userId, UpdateArchitectDto updateRequest) {
  //        // Find architect by user ID
  //        Architect architect =
  //                architectRepository
  //                        .findByUserId(userId)
  //                        .orElseThrow(
  //                                () -> new ResourceNotFoundException("Architect not found for
  // user ID: " + userId));
  //
  //        User user = architect.getUser();
  //
  //        // Update user fields if provided
  //        if (updateRequest.getEmail() != null &&
  // !updateRequest.getEmail().equals(user.getEmail())) {
  //            // Check if new email is already taken
  //            if (userRepository.existsByEmail(updateRequest.getEmail())) {
  //                throw new IllegalArgumentException("Email is already in use!");
  //            }
  //            user.setEmail(updateRequest.getEmail());
  //        }
  //
  //        if (updateRequest.getPassword() != null) {
  //            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
  //        }
  //
  //        user.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
  //        userRepository.save(user);
  //
  //        // Update architect fields if provided
  //        if (updateRequest.getCompanyName() != null) {
  //            architect.setCompanyName(updateRequest.getCompanyName());
  //        }
  //
  //        if (updateRequest.getCompanySite() != null) {
  //            architect.setCompanySite(updateRequest.getCompanySite());
  //        }
  //
  //        if (updateRequest.getContactName() != null) {
  //            architect.setContactName(updateRequest.getContactName());
  //        }
  //
  //        if (updateRequest.getKtpNum() != null
  //                && !updateRequest.getKtpNum().equals(architect.getKtpNum())) {
  //            // Check if new KTP number is already taken
  //            if (architectRepository.existsByKtpNum(updateRequest.getKtpNum())) {
  //                throw new IllegalArgumentException("KTP number is already registered!");
  //            }
  //            architect.setKtpNum(updateRequest.getKtpNum());
  //        }
  //
  //        if (updateRequest.getNpwp() != null &&
  // !updateRequest.getNpwp().equals(architect.getNpwp())) {
  //            // Check if new NPWP is already taken
  //            if (architectRepository.existsByNpwp(updateRequest.getNpwp())) {
  //                throw new IllegalArgumentException("NPWP is already registered!");
  //            }
  //            architect.setNpwp(updateRequest.getNpwp());
  //        }
  //
  //        architect = architectRepository.save(architect);
  //
  //        return mapToDto(architect);
  //    }
  //
  public ClientDto getClientByUserId(Long userId) {
    Client client =
        clientRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Architect not found for user ID: " + userId));
    return mapToDto(client);
  }

  private ClientDto mapToDto(Client client) {
    return ClientDto.builder()
        .id(client.getId())
        .userId(client.getUser().getId())
        .ktpNum(client.getKtpNum())
        .ktpVerified(client.isKtpVerified())
        .projectFinished(client.getProjectFinished())
        .projectMatch(client.getProjectMatch())
        .build();
  }
}
