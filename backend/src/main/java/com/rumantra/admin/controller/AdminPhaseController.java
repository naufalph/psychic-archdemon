package com.rumantra.admin.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.admin.dto.DisputeResolutionRequest;
import com.rumantra.admin.service.AdminPhaseService;
import com.rumantra.project.domain.ProjectPhase;
import com.rumantra.project.dto.PhaseResponse;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin - Phases")
@RestController
@RequestMapping("/rmtr/admin/phases")
@RequiredArgsConstructor
public class AdminPhaseController {

  private final AdminPhaseService adminPhaseService;

  @GetMapping("/disputed")
  public ResponseEntity<ApiResponse<List<PhaseResponse>>> getDisputedPhases() {
    List<PhaseResponse> phases =
        adminPhaseService.getDisputedPhases().stream()
            .map(this::toSimpleResponse)
            .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(phases));
  }

  @PostMapping("/{phaseId}/resolve-dispute")
  public ResponseEntity<ApiResponse<PhaseResponse>> resolveDispute(
      @PathVariable Long phaseId, @Valid @RequestBody DisputeResolutionRequest req) {
    Long superuserId = SecurityUtils.getCurrentUserId();
    ProjectPhase phase = adminPhaseService.resolveDispute(phaseId, superuserId, req);
    return ResponseEntity.ok(ApiResponse.success(toSimpleResponse(phase)));
  }

  private PhaseResponse toSimpleResponse(ProjectPhase phase) {
    return PhaseResponse.builder()
        .id(phase.getId())
        .projectId(phase.getProject().getId())
        .phaseNumber(phase.getPhaseNumber())
        .title(phase.getTitle())
        .description(phase.getDescription())
        .amount(phase.getAmount())
        .status(phase.getStatus().name())
        .dueDate(phase.getDueDate())
        .maxRevisions(phase.getMaxRevisions())
        .revisionsUsed(phase.getRevisionsUsed())
        .createdAt(phase.getCreatedAt())
        .updatedAt(phase.getUpdatedAt())
        .build();
  }
}
