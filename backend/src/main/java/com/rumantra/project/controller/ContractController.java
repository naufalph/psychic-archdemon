package com.rumantra.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.rumantra.project.dto.AcceptContractRequest;
import com.rumantra.project.dto.ContractAcceptanceStatusResponse;
import com.rumantra.project.dto.ContractDocumentResponse;
import com.rumantra.project.dto.ContractResponse;
import com.rumantra.project.service.ContractDocumentService;
import com.rumantra.project.service.ContractService;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ContractController {

  private final ContractService contractService;
  private final ContractDocumentService contractDocumentService;

  @Operation(summary = "Contract, payment schedule and transaction history for a project")
  @GetMapping("/rmtr/projects/{projectId}/contract")
  public ResponseEntity<ApiResponse<ContractResponse>> getContract(@PathVariable Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(ApiResponse.success(contractService.getContract(projectId, userId)));
  }

  @Operation(summary = "The signed agreement, snapshotted from the accepted bid on first read")
  @GetMapping("/rmtr/projects/{projectId}/contract/document")
  public ResponseEntity<ApiResponse<ContractDocumentResponse>> getDocument(
      @PathVariable Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        ApiResponse.success(contractDocumentService.getDocument(projectId, userId)));
  }

  @Operation(summary = "Which parties have signed the agreement")
  @GetMapping("/rmtr/projects/{projectId}/contract/acceptances")
  public ResponseEntity<ApiResponse<ContractAcceptanceStatusResponse>> getAcceptances(
      @PathVariable Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        ApiResponse.success(contractDocumentService.getStatus(projectId, userId)));
  }

  @Operation(summary = "Sign the agreement as the calling party")
  @PostMapping("/rmtr/projects/{projectId}/contract/accept")
  public ResponseEntity<ApiResponse<ContractAcceptanceStatusResponse>> accept(
      @PathVariable Long projectId,
      @Valid @RequestBody AcceptContractRequest request,
      HttpServletRequest servletRequest) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(
        ApiResponse.success(
            contractDocumentService.accept(
                projectId,
                userId,
                request,
                clientIp(servletRequest),
                servletRequest.getHeader("User-Agent"))));
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
