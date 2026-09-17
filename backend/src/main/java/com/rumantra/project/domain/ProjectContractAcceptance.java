package com.rumantra.project.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;
import lombok.*;

/**
 * One party's signature on a contract. An audit row, so userId is a plain column rather than an
 * association -- the same choice UserAgreementAcceptance makes.
 *
 * <p>Append-only like the contract itself: the database rejects UPDATE and DELETE, and
 * {@code @Immutable} stops Hibernate flushing one.
 */
@Builder
@Getter
@Setter
@Immutable
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_project_contract_acceptance")
public class ProjectContractAcceptance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "contract_id", nullable = false)
  private Long contractId;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "party", nullable = false, length = 16)
  private ContractParty party;

  @Column(name = "signature_name", nullable = false)
  private String signatureName;

  @Column(name = "content_hash", nullable = false, length = 64)
  private String contentHash;

  @Column(name = "lang", nullable = false, length = 8)
  private String lang;

  @Column(name = "accepted_at", nullable = false)
  @Builder.Default
  private Timestamp acceptedAt = Timestamp.valueOf(LocalDateTime.now());

  @Column(name = "ip_address", length = 64)
  private String ipAddress;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;
}
