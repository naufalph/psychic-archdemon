package com.rumantra.payment.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.architect.domain.Architect;
import com.rumantra.architect.repository.ArchitectRepository;
import com.rumantra.payment.domain.TokenPurchase;
import com.rumantra.payment.dto.TokenPurchaseDetailResponse;
import com.rumantra.payment.dto.TokenPurchaseInitiateRequest;
import com.rumantra.payment.dto.TokenPurchasePricingResponse;
import com.rumantra.payment.dto.TokenPurchaseResponse;
import com.rumantra.payment.service.TokenPurchaseService;
import com.rumantra.security.SecurityUtils;
import com.rumantra.shared.dto.ApiResponse;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.subscription.service.SubscriptionService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rmtr/tokens")
@Slf4j
public class TokenPurchaseController {

  @Autowired private TokenPurchaseService tokenPurchaseService;

  @Autowired private ArchitectRepository architectRepository;

  @Autowired private SubscriptionService subscriptionService;

  @PostMapping
  public ResponseEntity<ApiResponse<TokenPurchaseResponse>> initiatePurchase(
      @Valid @RequestBody TokenPurchaseInitiateRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new BusinessException(ExceptionConstants.UNAUTHORIZED_ARCHITECT_ACCESS));

    TokenPurchase purchase =
        tokenPurchaseService.initiatePurchase(architect, request.getQuantity());

    TokenPurchaseResponse response =
        TokenPurchaseResponse.builder()
            .purchaseId(purchase.getId())
            .quantity(purchase.getQuantity())
            .pricePerToken(purchase.getPricePerToken())
            .totalAmount(purchase.getTotalAmount())
            .paymentLink(purchase.getPaymentLink())
            .expiresAt(purchase.getExpiresAt())
            .status(purchase.getStatus().name())
            .build();

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.<TokenPurchaseResponse>builder()
                .success(true)
                .message("Token purchase initiated")
                .data(response)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<TokenPurchaseDetailResponse>> getPurchaseById(
      @PathVariable Long id) {
    Long userId = SecurityUtils.getCurrentUserId();
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new BusinessException(ExceptionConstants.UNAUTHORIZED_ARCHITECT_ACCESS));

    TokenPurchase purchase = tokenPurchaseService.getPurchaseById(architect.getId(), id);

    TokenPurchaseDetailResponse response =
        TokenPurchaseDetailResponse.builder()
            .id(purchase.getId())
            .quantity(purchase.getQuantity())
            .pricePerToken(purchase.getPricePerToken())
            .totalAmount(purchase.getTotalAmount())
            .tier(purchase.getTier().name())
            .status(purchase.getStatus().name())
            .paymentMethod(purchase.getPaymentMethod())
            .paymentChannel(purchase.getPaymentChannel())
            .createdAt(purchase.getCreatedAt())
            .completedAt(purchase.getCompletedAt())
            .expiresAt(purchase.getExpiresAt())
            .failureReason(purchase.getFailureReason())
            .build();

    return ResponseEntity.ok(
        ApiResponse.<TokenPurchaseDetailResponse>builder()
            .success(true)
            .message("Purchase details retrieved")
            .data(response)
            .timestamp(java.time.LocalDateTime.now().toString())
            .build());
  }

  @GetMapping("/history")
  public ResponseEntity<ApiResponse<Page<TokenPurchaseDetailResponse>>> getPurchaseHistory(
      Pageable pageable) {
    Long userId = SecurityUtils.getCurrentUserId();
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new BusinessException(ExceptionConstants.UNAUTHORIZED_ARCHITECT_ACCESS));

    Page<TokenPurchaseDetailResponse> purchases =
        tokenPurchaseService
            .getPurchaseHistory(architect.getId(), pageable)
            .map(
                purchase ->
                    TokenPurchaseDetailResponse.builder()
                        .id(purchase.getId())
                        .quantity(purchase.getQuantity())
                        .pricePerToken(purchase.getPricePerToken())
                        .totalAmount(purchase.getTotalAmount())
                        .tier(purchase.getTier().name())
                        .status(purchase.getStatus().name())
                        .paymentMethod(purchase.getPaymentMethod())
                        .paymentChannel(purchase.getPaymentChannel())
                        .createdAt(purchase.getCreatedAt())
                        .completedAt(purchase.getCompletedAt())
                        .expiresAt(purchase.getExpiresAt())
                        .failureReason(purchase.getFailureReason())
                        .build());

    return ResponseEntity.ok(
        ApiResponse.<Page<TokenPurchaseDetailResponse>>builder()
            .success(true)
            .message("Purchase history retrieved")
            .data(purchases)
            .timestamp(java.time.LocalDateTime.now().toString())
            .build());
  }

  @GetMapping("/pricing")
  public ResponseEntity<ApiResponse<TokenPurchasePricingResponse>> getPricing() {
    Long userId = SecurityUtils.getCurrentUserId();
    Architect architect =
        architectRepository
            .findByUserId(userId)
            .orElseThrow(
                () -> new BusinessException(ExceptionConstants.UNAUTHORIZED_ARCHITECT_ACCESS));

    Map<String, Object> pricingInfo =
        tokenPurchaseService.getPricingInfo(
            subscriptionService.getActiveSubscription(architect.getId()).getTier());

    TokenPurchasePricingResponse response =
        TokenPurchasePricingResponse.builder()
            .currentTier((String) pricingInfo.get("currentTier"))
            .pricePerToken((java.math.BigDecimal) pricingInfo.get("pricePerToken"))
            .currency((String) pricingInfo.get("currency"))
            .minQuantity((Integer) pricingInfo.get("minQuantity"))
            .maxQuantity((Integer) pricingInfo.get("maxQuantity"))
            .tierPricing((Map<String, java.math.BigDecimal>) pricingInfo.get("tierPricing"))
            .build();

    return ResponseEntity.ok(
        ApiResponse.<TokenPurchasePricingResponse>builder()
            .success(true)
            .message("Pricing information retrieved")
            .data(response)
            .timestamp(java.time.LocalDateTime.now().toString())
            .build());
  }
}
