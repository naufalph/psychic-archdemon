package com.rumantra.bidding.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidDetail;
import com.rumantra.bidding.dto.BidDetailRequest;
import com.rumantra.bidding.dto.BidDetailResponse;
import com.rumantra.bidding.repository.BidDetailRepository;

@Service
public class BidDetailService {

  @Autowired private BidDetailRepository bidDetailRepository;

  @Transactional
  public BidDetail createOrUpdate(Bid bid, BidDetailRequest request) {
    BidDetail detail =
        bidDetailRepository.findByBidId(bid.getId()).orElse(BidDetail.builder().bid(bid).build());

    if (request.getConceptStatement() != null) {
      validateConceptStatement(request.getConceptStatement());
      detail.setConceptStatement(request.getConceptStatement());
    }
    if (request.getDeliverables() != null) {
      detail.setDeliverables(request.getDeliverables());
    }
    if (request.getSiteAnalysisRevisions() != null) {
      detail.setSiteAnalysisRevisions(request.getSiteAnalysisRevisions());
    }
    if (request.getDesignRevisions() != null) {
      detail.setDesignRevisions(request.getDesignRevisions());
    }
    if (request.getPermitsDocRevisions() != null) {
      detail.setPermitsDocRevisions(request.getPermitsDocRevisions());
    }
    if (request.getSpecializedServicesRevisions() != null) {
      detail.setSpecializedServicesRevisions(request.getSpecializedServicesRevisions());
    }
    if (request.getConstructionSupportRevisions() != null) {
      detail.setConstructionSupportRevisions(request.getConstructionSupportRevisions());
    }

    return bidDetailRepository.save(detail);
  }

  public BidDetailResponse getDetailResponse(Long bidId) {
    return bidDetailRepository
        .findByBidId(bidId)
        .map(
            detail ->
                BidDetailResponse.builder()
                    .id(detail.getId())
                    .conceptStatement(detail.getConceptStatement())
                    .deliverables(detail.getDeliverables())
                    .siteAnalysisRevisions(detail.getSiteAnalysisRevisions())
                    .designRevisions(detail.getDesignRevisions())
                    .permitsDocRevisions(detail.getPermitsDocRevisions())
                    .specializedServicesRevisions(detail.getSpecializedServicesRevisions())
                    .constructionSupportRevisions(detail.getConstructionSupportRevisions())
                    .build())
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

  public boolean hasRequiredDetails(Long bidId) {
    return bidDetailRepository
        .findByBidId(bidId)
        .map(
            detail ->
                detail.getConceptStatement() != null && !detail.getConceptStatement().isEmpty())
        .orElse(false);
  }
}
