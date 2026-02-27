package com.rumantra.bidding.domain;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_bid_detail")
public class BidDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bid_id", nullable = false, unique = true)
  @ToString.Exclude
  private Bid bid;

  @Column(name = "concept_statement", columnDefinition = "TEXT")
  private String conceptStatement;

  @Type(JsonType.class)
  @Column(name = "deliverables", columnDefinition = "jsonb")
  private List<String> deliverables;

  @Column(name = "site_analysis_revisions")
  private Integer siteAnalysisRevisions;

  @Column(name = "design_revisions")
  private Integer designRevisions;

  @Column(name = "permits_doc_revisions")
  private Integer permitsDocRevisions;

  @Column(name = "specialized_services_revisions")
  private Integer specializedServicesRevisions;

  @Column(name = "construction_support_revisions")
  private Integer constructionSupportRevisions;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
