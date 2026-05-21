package com.rumantra.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.admin.dto.AdminUserDto;
import com.rumantra.admin.service.AdminUserService;
import com.rumantra.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Users")
@RestController
@RequestMapping("/rmtr/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

  private final AdminUserService adminUserService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<AdminUserDto>>> getUsers(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(ApiResponse.success(adminUserService.getUsers(page, size)));
  }

  @PostMapping("/{userId}/deactivate")
  public ResponseEntity<ApiResponse<AdminUserDto>> deactivateUser(@PathVariable Long userId) {
    return ResponseEntity.ok(ApiResponse.success(adminUserService.deactivateUser(userId)));
  }

  @PostMapping("/{userId}/reactivate")
  public ResponseEntity<ApiResponse<AdminUserDto>> reactivateUser(@PathVariable Long userId) {
    return ResponseEntity.ok(ApiResponse.success(adminUserService.reactivateUser(userId)));
  }
}
