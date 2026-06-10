package com.rumantra.payment.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.payment.dto.PhasePaymentInitiateResponse;
import com.rumantra.payment.dto.PhasePaymentResponse;
import com.rumantra.payment.service.PaymentService;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rmtr/payments")
@Slf4j
public class PaymentController {

  @Autowired private PaymentService paymentService;

  @GetMapping("/projects/{projectId}")
  public ResponseEntity<ApiResponse<List<PhasePaymentResponse>>> getProjectPhasePayments(
      @PathVariable Long projectId) {
    Long userId = SecurityUtils.getCurrentUserId();
    List<PhasePaymentResponse> phases = paymentService.getProjectPhasePayments(projectId, userId);
    return ResponseEntity.ok(
        ApiResponse.<List<PhasePaymentResponse>>builder()
            .success(true)
            .message("Phase payments retrieved")
            .data(phases)
            .timestamp(java.time.LocalDateTime.now().toString())
            .build());
  }

  @PostMapping("/phases/{phaseId}")
  public ResponseEntity<ApiResponse<PhasePaymentInitiateResponse>> initiatePhasePayment(
      @PathVariable Long phaseId) {
    Long userId = SecurityUtils.getCurrentUserId();
    PhasePaymentInitiateResponse response = paymentService.initiatePhasePayment(phaseId, userId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.<PhasePaymentInitiateResponse>builder()
                .success(true)
                .message("Phase payment initiated")
                .data(response)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build());
  }
}
