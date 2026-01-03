package com.rumantra.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.architect.domain.Architect;
import com.rumantra.bidding.service.BidQuotaService;
import com.rumantra.bidding.service.BidUsageLogService;
import com.rumantra.integration.xendit.XenditService;
import com.rumantra.integration.xendit.dto.XenditPaymentRequestRequest;
import com.rumantra.integration.xendit.dto.XenditPaymentResponse;
import com.rumantra.integration.xendit.dto.XenditPaymentWebhook;
import com.rumantra.payment.domain.PurchaseStatus;
import com.rumantra.payment.domain.TokenPurchase;
import com.rumantra.payment.repository.TokenPurchaseRepository;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.subscription.domain.SubscriptionTier;
import com.rumantra.subscription.service.SubscriptionService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TokenPurchaseService {

  @Autowired private TokenPurchaseRepository tokenPurchaseRepository;

  @Autowired private BidQuotaService bidQuotaService;

  @Autowired private BidUsageLogService bidUsageLogService;

  @Autowired private SubscriptionService subscriptionService;

  @Autowired private XenditService xenditService;

  private static final BigDecimal FREE_TIER_PRICE_PER_TOKEN = new BigDecimal("400000");
  private static final BigDecimal BASIC_TIER_PRICE_PER_TOKEN = new BigDecimal("250000");
  private static final int MIN_QUANTITY = 1;
  private static final int MAX_QUANTITY = 50;

  @Transactional
  public TokenPurchase initiatePurchase(Architect architect, Integer quantity) {
    if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
      throw new BusinessException(ExceptionConstants.INVALID_PURCHASE_QUANTITY);
    }

    SubscriptionTier tier = subscriptionService.getActiveSubscription(architect.getId()).getTier();

    BigDecimal pricePerToken = calculatePricePerToken(tier);
    BigDecimal totalAmount = pricePerToken.multiply(new BigDecimal(quantity));

    String referenceId = generateReferenceId(architect.getId());

    Map<String, String> metadata = new HashMap<>();
    metadata.put("architect_id", architect.getId().toString());
    metadata.put("quantity", quantity.toString());
    metadata.put("tier", tier.name());

    XenditPaymentRequestRequest xenditRequest =
        XenditPaymentRequestRequest.builder()
            .referenceId(referenceId)
            .requestAmount(totalAmount)
            .description(String.format("Purchase %d bid token(s)", quantity))
            .metadata(metadata)
            .build();

    XenditPaymentResponse xenditResponse = xenditService.createPaymentRequest(xenditRequest);

    TokenPurchase purchase =
        TokenPurchase.builder()
            .architect(architect)
            .quantity(quantity)
            .pricePerToken(pricePerToken)
            .totalAmount(totalAmount)
            .tier(tier)
            .status(PurchaseStatus.PENDING)
            .xenditPaymentRequestId(xenditResponse.getId())
            .xenditReferenceId(referenceId)
            .paymentLink(extractPaymentLink(xenditResponse))
            .expiresAt(LocalDateTime.now().plusHours(24))
            .build();

    return tokenPurchaseRepository.save(purchase);
  }

  @Transactional
  public void handlePaymentSucceeded(XenditPaymentWebhook webhook) {
    String referenceId = webhook.getData().getReferenceId();
    TokenPurchase purchase =
        tokenPurchaseRepository
            .findByXenditReferenceId(referenceId)
            .orElseThrow(() -> new RuntimeException("Purchase not found: " + referenceId));

    if (purchase.getStatus() == PurchaseStatus.COMPLETED) {
      log.info("Purchase already completed, skipping: {}", purchase.getId());
      return;
    }

    if (purchase.getXenditPaymentRequestId() != null
        && purchase.getXenditPaymentRequestId().equals(webhook.getData().getId())) {
      log.info("Duplicate webhook for purchase {}, already processed", purchase.getId());
      return;
    }

    purchase.setStatus(PurchaseStatus.COMPLETED);
    purchase.setXenditPaymentRequestId(webhook.getData().getId());
    purchase.setCompletedAt(LocalDateTime.now());

    if (webhook.getData().getPaymentMethod() != null) {
      purchase.setPaymentMethod(webhook.getData().getPaymentMethod().getType());
      purchase.setPaymentChannel(extractPaymentChannel(webhook.getData().getPaymentMethod()));
    }

    tokenPurchaseRepository.save(purchase);

    bidQuotaService.allocateTokens(purchase.getArchitect().getId(), purchase.getQuantity());

    bidUsageLogService.logTokenPurchase(
        purchase.getArchitect(), purchase.getQuantity(), purchase.getTotalAmount());

    log.info(
        "Token purchase completed: {} tokens allocated to architect {}",
        purchase.getQuantity(),
        purchase.getArchitect().getId());
  }

  @Transactional
  public void handlePaymentFailed(XenditPaymentWebhook webhook) {
    String referenceId = webhook.getData().getReferenceId();
    TokenPurchase purchase =
        tokenPurchaseRepository
            .findByXenditReferenceId(referenceId)
            .orElseThrow(() -> new RuntimeException("Purchase not found: " + referenceId));

    purchase.setStatus(PurchaseStatus.FAILED);
    purchase.setFailureReason(webhook.getData().getStatus());
    tokenPurchaseRepository.save(purchase);

    log.warn("Payment failed for purchase {}", purchase.getId());
  }

  @Transactional
  public void handlePaymentExpired(XenditPaymentWebhook webhook) {
    String referenceId = webhook.getData().getReferenceId();
    TokenPurchase purchase =
        tokenPurchaseRepository
            .findByXenditReferenceId(referenceId)
            .orElseThrow(() -> new RuntimeException("Purchase not found: " + referenceId));

    purchase.setStatus(PurchaseStatus.EXPIRED);
    tokenPurchaseRepository.save(purchase);

    log.info("Payment expired for purchase {}", purchase.getId());
  }

  public TokenPurchase getPurchaseById(Long architectId, Long purchaseId) {
    TokenPurchase purchase =
        tokenPurchaseRepository
            .findById(purchaseId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.PURCHASE_NOT_FOUND));

    if (!purchase.getArchitect().getId().equals(architectId)) {
      throw new BusinessException(ExceptionConstants.UNAUTHORIZED_PURCHASE_ACCESS);
    }

    return purchase;
  }

  public Page<TokenPurchase> getPurchaseHistory(Long architectId, Pageable pageable) {
    return tokenPurchaseRepository.findByArchitectIdOrderByCreatedAtDesc(architectId, pageable);
  }

  public Map<String, Object> getPricingInfo(SubscriptionTier tier) {
    Map<String, Object> pricingInfo = new HashMap<>();
    pricingInfo.put("currentTier", tier.name());
    pricingInfo.put("pricePerToken", calculatePricePerToken(tier));
    pricingInfo.put("currency", "IDR");
    pricingInfo.put("minQuantity", MIN_QUANTITY);
    pricingInfo.put("maxQuantity", MAX_QUANTITY);

    Map<String, BigDecimal> tierPricing = new HashMap<>();
    tierPricing.put("FREE", FREE_TIER_PRICE_PER_TOKEN);
    tierPricing.put("BASIC", BASIC_TIER_PRICE_PER_TOKEN);
    pricingInfo.put("tierPricing", tierPricing);

    return pricingInfo;
  }

  private BigDecimal calculatePricePerToken(SubscriptionTier tier) {
    return tier == SubscriptionTier.BASIC ? BASIC_TIER_PRICE_PER_TOKEN : FREE_TIER_PRICE_PER_TOKEN;
  }

  private String generateReferenceId(Long architectId) {
    return String.format("token_purchase_arch_%d_%d", architectId, System.currentTimeMillis());
  }

  private String extractPaymentLink(XenditPaymentResponse response) {
    if (response.getActions() != null && !response.getActions().isEmpty()) {
      return response.getActions().stream()
          .filter(action -> "WEB".equals(action.getUrlType()))
          .findFirst()
          .map(action -> action.getUrl())
          .orElse(null);
    }
    return null;
  }

  private String extractPaymentChannel(XenditPaymentWebhook.PaymentMethod paymentMethod) {
    if (paymentMethod.getEwallet() != null) {
      return paymentMethod.getEwallet().getChannelCode();
    } else if (paymentMethod.getCard() != null) {
      return paymentMethod.getCard().getCardBrand();
    } else if (paymentMethod.getDirectDebit() != null) {
      return paymentMethod.getDirectDebit().getChannelCode();
    } else if (paymentMethod.getOverTheCounter() != null) {
      return paymentMethod.getOverTheCounter().getChannelCode();
    } else if (paymentMethod.getVirtualAccount() != null) {
      return paymentMethod.getVirtualAccount().getChannelCode();
    } else if (paymentMethod.getQrCode() != null) {
      return paymentMethod.getQrCode().getChannelCode();
    }
    return null;
  }
}
