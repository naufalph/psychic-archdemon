package com.rumantra.client.service;

import org.springframework.stereotype.Service;

import com.rumantra.client.domain.Client;
import com.rumantra.client.dto.ClientDto;
import com.rumantra.client.dto.ClientSignupRequestDto;
import com.rumantra.client.dto.UpdateClientProfileRequest;
import com.rumantra.client.repository.ClientRepository;
import com.rumantra.shared.constants.ErrorCodes;
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

  public ClientDto getClientByUserId(Long userId) {
    Client client =
        clientRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Client not found for user ID: " + userId));
    return mapToDto(client);
  }

  @Transactional
  public ClientDto updateClientProfile(Long userId, UpdateClientProfileRequest request) {
    Client client =
        clientRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Client not found for user ID: " + userId));

    if (client.isKtpVerified()) {
      if (request.getFirstName() != null
          && !request.getFirstName().equals(client.getUser().getFirstName())) {
        throw new IllegalStateException(ErrorCodes.NAME_VERIFIED_CANNOT_MODIFY);
      }
      if (request.getLastName() != null
          && !request.getLastName().equals(client.getUser().getLastName())) {
        throw new IllegalStateException(ErrorCodes.NAME_VERIFIED_CANNOT_MODIFY);
      }
      if (request.getKtpNum() != null && !request.getKtpNum().equals(client.getKtpNum())) {
        throw new IllegalStateException(ErrorCodes.KTP_VERIFIED_CANNOT_MODIFY);
      }
    } else {
      if (request.getFirstName() != null) {
        if (request.getFirstName().trim().isEmpty()) {
          throw new IllegalArgumentException(ErrorCodes.FIRST_NAME_REQUIRED);
        }
        client.getUser().setFirstName(request.getFirstName());
      }
      if (request.getLastName() != null) {
        if (request.getLastName().trim().isEmpty()) {
          throw new IllegalArgumentException(ErrorCodes.LAST_NAME_REQUIRED);
        }
        client.getUser().setLastName(request.getLastName());
      }
    }

    client.setPhoneNumber(request.getPhoneNumber());
    if (request.getKtpNum() != null) {
      client.setKtpNum(request.getKtpNum());
    }

    client = clientRepository.save(client);

    return mapToDto(client);
  }

  private ClientDto mapToDto(Client client) {
    return ClientDto.builder()
        .userName(client.getUser().getFirstName())
        .firstName(client.getUser().getFirstName())
        .lastName(client.getUser().getLastName())
        .email(client.getUser().getEmail())
        .phoneNumber(client.getPhoneNumber())
        .ktpNum(client.getKtpNum())
        .ktpVerified(client.isKtpVerified())
        .projectFinished(client.getProjectFinished())
        .projectMatch(client.getProjectMatch())
        .build();
  }
}
