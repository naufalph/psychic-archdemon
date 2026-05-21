package com.rumantra.admin.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.admin.dto.AdminUserDto;
import com.rumantra.shared.exception.ResourceNotFoundException;
import com.rumantra.user.domain.User;
import com.rumantra.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public Page<AdminUserDto> getUsers(int page, int size) {
    return userRepository
        .findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
        .map(this::toDto);
  }

  @Transactional
  public AdminUserDto deactivateUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    user.setActive(false);
    return toDto(userRepository.save(user));
  }

  @Transactional
  public AdminUserDto reactivateUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    user.setActive(true);
    return toDto(userRepository.save(user));
  }

  private AdminUserDto toDto(User user) {
    List<String> roles = new ArrayList<>();
    if (user.getArchitect() != null) roles.add("ARCHITECT");
    if (user.getClient() != null) roles.add("CLIENT");
    if (user.isSuperuser()) roles.add("SUPERUSER");

    return AdminUserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .isEmailVerified(user.isEmailVerified())
        .isActive(user.isActive())
        .isSuperuser(user.isSuperuser())
        .createdAt(user.getCreatedAt())
        .roles(roles)
        .build();
  }
}
