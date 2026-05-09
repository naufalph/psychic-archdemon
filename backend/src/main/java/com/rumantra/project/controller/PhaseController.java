package com.rumantra.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.payment.domain.PhasePayment;
import com.rumantra.project.dto.DeliverableResponse;
import com.rumantra.project.dto.DeliverableUploadRequest;
import com.rumantra.project.dto.DisbursementRequest;
import com.rumantra.project.dto.DisbursementResponse;
import com.rumantra.project.dto.DisputeRequest;
import com.rumantra.project.dto.PhaseCreateRequest;
import com.rumantra.project.dto.PhaseLogResponse;
import com.rumantra.project.dto.PhaseResponse;
import com.rumantra.project.service.PhasePaymentService;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Phase Payment")
@RestController
public class PhaseController {

  @Autowired private PhasePaymentService phasePaymentService;

  @Operation(summary = "Create a new phase for an IN_PROGRESS project")
  @PostMapping("/rmtr/projects/{projectId}/phases")
  public ResponseEntity<ApiResponse<PhaseResponse>> initializePhase(
      @PathVariable Long projectId, @Valid @RequestBody PhaseCreateRequest req) {
    Long userId = SecurityUtils.getCurrentUserId();
    PhaseResponse phase = phasePaymentService.initializePhase(projectId, userId, req);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(phase));
  }

  @Operation(summary = "List all phases for a project")
  @GetMapping("/rmtr/projects/{projectId}/phases")
  public ResponseEntity<ApiResponse<List<PhaseResponse>>> listPhases(@PathVariable Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();
    List<PhaseResponse> phases = phasePaymentService.listPhases(projectId, userId);
    return ResponseEntity.ok(ApiResponse.success(phases));
  }

  @Operation(summary = "Get a single phase")
  @GetMapping("/rmtr/projects/{projectId}/phases/{phaseId}")
  public ResponseEntity<ApiResponse<PhaseResponse>> getPhase(
      @PathVariable Long projectId, @PathVariable Long phaseId) {
    Long userId = SecurityUtils.getCurrentUserId();
    PhaseResponse phase = phasePaymentService.getPhase(phaseId, userId);
    return ResponseEntity.ok(ApiResponse.success(phase));
  }

  @Operation(summary = "Bill client for a phase — creates Xendit invoice")
  @PostMapping("/rmtr/phases/{phaseId}/bill")
  public ResponseEntity<ApiResponse<PhaseResponse>> billPhase(@PathVariable Long phaseId) {
    Long userId = SecurityUtils.getCurrentUserId();
    PhasePayment payment = phasePaymentService.createInvoiceForPhase(phaseId, userId);
    PhaseResponse phase = phasePaymentService.getPhase(phaseId, userId);
    return ResponseEntity.ok(ApiResponse.success(phase));
  }

  @Operation(summary = "Architect uploads a deliverable for a phase")
  @PostMapping("/rmtr/phases/{phaseId}/deliverables")
  public ResponseEntity<ApiResponse<DeliverableResponse>> addDeliverable(
      @PathVariable Long phaseId, @Valid @RequestBody DeliverableUploadRequest req) {
    Long userId = SecurityUtils.getCurrentUserId();
    DeliverableResponse deliverable = phasePaymentService.addDeliverable(phaseId, userId, req);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(deliverable));
  }

  @Operation(summary = "Client approves the deliverable")
  @PostMapping("/rmtr/phases/{phaseId}/approve")
  public ResponseEntity<ApiResponse<PhaseResponse>> approveDeliverable(@PathVariable Long phaseId) {
    Long userId = SecurityUtils.getCurrentUserId();
    PhaseResponse phase = phasePaymentService.approveDeliverable(phaseId, userId);
    return ResponseEntity.ok(ApiResponse.success(phase));
  }

  @Operation(summary = "Client disputes the deliverable")
  @PostMapping("/rmtr/phases/{phaseId}/dispute")
  public ResponseEntity<ApiResponse<PhaseResponse>> disputeDeliverable(
      @PathVariable Long phaseId, @Valid @RequestBody DisputeRequest req) {
    Long userId = SecurityUtils.getCurrentUserId();
    PhaseResponse phase = phasePaymentService.disputeDeliverable(phaseId, userId, req.getReason());
    return ResponseEntity.ok(ApiResponse.success(phase));
  }

  @Operation(summary = "Architect initiates payout disbursement")
  @PostMapping("/rmtr/phases/{phaseId}/disburse")
  public ResponseEntity<ApiResponse<DisbursementResponse>> initiateDisbursement(
      @PathVariable Long phaseId, @Valid @RequestBody DisbursementRequest req) {
    Long userId = SecurityUtils.getCurrentUserId();
    DisbursementResponse disbursement =
        phasePaymentService.initiateDisbursement(phaseId, userId, req);
    return ResponseEntity.ok(ApiResponse.success(disbursement));
  }

  @Operation(summary = "Get the audit log for a phase")
  @GetMapping("/rmtr/phases/{phaseId}/logs")
  public ResponseEntity<ApiResponse<List<PhaseLogResponse>>> getLogs(@PathVariable Long phaseId) {
    Long userId = SecurityUtils.getCurrentUserId();
    List<PhaseLogResponse> logs = phasePaymentService.listLogs(phaseId, userId);
    return ResponseEntity.ok(ApiResponse.success(logs));
  }
}
