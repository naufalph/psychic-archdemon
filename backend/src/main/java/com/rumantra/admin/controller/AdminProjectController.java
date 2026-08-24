package com.rumantra.admin.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.admin.dto.AdminNegotiationResolutionRequest;
import com.rumantra.admin.dto.AdminProjectDetailResponse;
import com.rumantra.admin.dto.NegotiationDisputeResponse;
import com.rumantra.admin.service.AdminProjectService;
import com.rumantra.client.domain.ProjectStatus;
import com.rumantra.client.dto.ProjectResponse;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Projects")
@RestController
@RequestMapping("/rmtr/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {

  private final AdminProjectService adminProjectService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(
      @RequestParam(required = false) ProjectStatus status) {
    return ResponseEntity.ok(ApiResponse.success(adminProjectService.getProjects(status)));
  }

  @GetMapping("/{projectId}")
  public ResponseEntity<ApiResponse<AdminProjectDetailResponse>> getProjectDetail(
      @PathVariable Long projectId) {
    return ResponseEntity.ok(ApiResponse.success(adminProjectService.getProjectDetail(projectId)));
  }

  @PostMapping("/{projectId}/force-cancel")
  public ResponseEntity<ApiResponse<ProjectResponse>> forceCancel(@PathVariable Long projectId) {
    return ResponseEntity.ok(ApiResponse.success(adminProjectService.forceCancel(projectId)));
  }

  @PostMapping("/{projectId}/override-negotiation")
  public ResponseEntity<ApiResponse<ProjectResponse>> overrideNegotiation(
      @PathVariable Long projectId) {
    return ResponseEntity.ok(
        ApiResponse.success(adminProjectService.overrideNegotiation(projectId)));
  }

  @GetMapping("/negotiation-disputes")
  public ResponseEntity<ApiResponse<List<NegotiationDisputeResponse>>> getNegotiationDisputes() {
    return ResponseEntity.ok(ApiResponse.success(adminProjectService.getNegotiationDisputes()));
  }

  @PostMapping("/{projectId}/resolve-negotiation-dispute")
  public ResponseEntity<ApiResponse<ProjectResponse>> resolveNegotiationDispute(
      @PathVariable Long projectId, @Valid @RequestBody AdminNegotiationResolutionRequest req) {
    Long superuserId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        ApiResponse.success(
            adminProjectService.resolveNegotiationDispute(projectId, superuserId, req)));
  }
}
