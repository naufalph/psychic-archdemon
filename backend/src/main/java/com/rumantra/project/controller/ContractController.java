package com.rumantra.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rumantra.project.dto.ContractResponse;
import com.rumantra.project.service.ContractService;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ContractController {

  private final ContractService contractService;

  @Operation(summary = "Contract, payment schedule and transaction history for a project")
  @GetMapping("/rmtr/projects/{projectId}/contract")
  public ResponseEntity<ApiResponse<ContractResponse>> getContract(@PathVariable Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();
    return ResponseEntity.ok(ApiResponse.success(contractService.getContract(projectId, userId)));
  }
}
