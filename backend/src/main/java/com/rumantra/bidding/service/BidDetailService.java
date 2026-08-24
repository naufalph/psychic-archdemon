package com.rumantra.bidding.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidDetail;
import com.rumantra.bidding.domain.BidPaymentPhase;
import com.rumantra.bidding.dto.BidDetailRequest;
import com.rumantra.bidding.dto.BidDetailResponse;
import com.rumantra.bidding.dto.BidPaymentPhaseRequest;
import com.rumantra.bidding.dto.BidPaymentPhaseResponse;
import com.rumantra.bidding.repository.BidDetailRepository;
import com.rumantra.bidding.repository.BidPaymentPhaseRepository;
import com.rumantra.bidding.repository.BidRepository;

@Service
public class BidDetailService {

  @Autowired private BidDetailRepository bidDetailRepository;

  @Autowired private BidPaymentPhaseRepository bidPaymentPhaseRepository;

  @Autowired private BidRepository bidRepository;

  @Transactional
  public BidDetail createOrUpdate(Bid bid, BidDetailRequest request) {
    BidDetail detail =
        bidDetailRepository.findByBidId(bid.getId()).orElse(BidDetail.builder().bid(bid).build());

    if (request.getConceptStatement() != null) {
      detail.setConceptStatement(request.getConceptStatement());
    }
    if (request.getFacadeDescription() != null) {
      detail.setFacadeDescription(request.getFacadeDescription());
    }
    if (request.getInteriorDescription() != null) {
      detail.setInteriorDescription(request.getInteriorDescription());
    }
    if (request.getMassingDescription() != null) {
      detail.setMassingDescription(request.getMassingDescription());
    }
    if (request.getZoningDescription() != null) {
      detail.setZoningDescription(request.getZoningDescription());
    }

    detail = bidDetailRepository.save(detail);

    if (request.getPhases() != null) {
      savePhases(bid, request.getPhases());
    }

    return detail;
  }

  public void validateForSubmission(Bid bid) {
    BidDetail detail =
        bidDetailRepository
            .findByBidId(bid.getId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Concept statement is required before submission"));
    validateConceptStatement(detail.getConceptStatement());
    if (detail.getConceptStatement() == null || detail.getConceptStatement().trim().isEmpty()) {
      throw new IllegalArgumentException("Concept statement is required before submission");
    }

    List<BidPaymentPhase> phases =
        bidPaymentPhaseRepository.findByBidIdOrderByPhaseNumber(bid.getId());
    if (phases.isEmpty()) {
      throw new IllegalArgumentException(
          "At least one payment phase is required before submission");
    }

    BigDecimal total =
        phases.stream()
            .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (total.compareTo(bid.getBidAmount()) != 0) {
      throw new IllegalArgumentException(
          "Sum of phase amounts ("
              + total
              + ") must equal bid amount ("
              + bid.getBidAmount()
              + ")");
    }

    boolean anyMissingDays =
        phases.stream().anyMatch(p -> p.getEstimatedDays() == null || p.getEstimatedDays() <= 0);
    if (anyMissingDays) {
      throw new IllegalArgumentException("Each phase must have a positive estimated_days value");
    }
  }

  public BidDetailResponse getDetailResponse(Long bidId) {
    return bidDetailRepository
        .findByBidId(bidId)
        .map(
            detail -> {
              List<BidPaymentPhaseResponse> phases = getPhaseResponses(bidId);
              return BidDetailResponse.builder()
                  .id(detail.getId())
                  .conceptStatement(detail.getConceptStatement())
                  .phases(phases)
                  .facadeDescription(detail.getFacadeDescription())
                  .interiorDescription(detail.getInteriorDescription())
                  .massingDescription(detail.getMassingDescription())
                  .zoningDescription(detail.getZoningDescription())
                  .build();
            })
        .orElse(null);
  }

  private void validateConceptStatement(String conceptStatement) {
    if (conceptStatement == null) return;

    String[] words = conceptStatement.trim().split("\\s+");
    if (words.length > 200) {
      throw new IllegalArgumentException(
          "Concept statement exceeds 200 words limit. Current: " + words.length);
    }
  }

  private void savePhases(Bid bid, List<BidPaymentPhaseRequest> phases) {
    bidPaymentPhaseRepository.deleteByBidId(bid.getId());

    int order = 0;
    for (BidPaymentPhaseRequest phaseReq : phases) {
      BidPaymentPhase phase =
          BidPaymentPhase.builder()
              .bid(bid)
              .phaseNumber(phaseReq.getPhaseNumber())
              .title(phaseReq.getTitle())
              .deliverables(phaseReq.getDeliverables())
              .amount(phaseReq.getAmount() != null ? phaseReq.getAmount() : BigDecimal.ZERO)
              .revisionRounds(phaseReq.getRevisionRounds())
              .estimatedDays(phaseReq.getEstimatedDays() != null ? phaseReq.getEstimatedDays() : 0)
              .displayOrder(order++)
              .build();

      bidPaymentPhaseRepository.save(phase);
    }

    int totalDays =
        phases.stream()
            .mapToInt(p -> p.getEstimatedDays() != null ? p.getEstimatedDays() : 0)
            .sum();
    if (totalDays > 0) {
      bid.setProposedTimelineDays(totalDays);
      bidRepository.save(bid);
    }
  }

  private List<BidPaymentPhaseResponse> getPhaseResponses(Long bidId) {
    List<BidPaymentPhase> phases = bidPaymentPhaseRepository.findByBidIdOrderByPhaseNumber(bidId);
    if (phases.isEmpty()) return Collections.emptyList();

    return phases.stream()
        .map(
            p ->
                BidPaymentPhaseResponse.builder()
                    .id(p.getId())
                    .phaseNumber(p.getPhaseNumber())
                    .title(p.getTitle())
                    .deliverables(p.getDeliverables())
                    .amount(p.getAmount())
                    .revisionRounds(p.getRevisionRounds())
                    .estimatedDays(p.getEstimatedDays())
                    .displayOrder(p.getDisplayOrder())
                    .build())
        .collect(Collectors.toList());
  }
}
