package com.rumantra.legal.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_user_agreement_acceptance")
public class UserAgreementAcceptance {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "doc_type", nullable = false)
  private LegalDocType docType;

  @Column(name = "version", nullable = false)
  private String version;

  @Column(name = "content_hash", nullable = false)
  private String contentHash;

  @Column(name = "lang", nullable = false)
  private String lang;

  @Column(name = "accepted_at", nullable = false)
  @Builder.Default
  private Timestamp acceptedAt = Timestamp.valueOf(LocalDateTime.now());

  @Column(name = "ip_address")
  private String ipAddress;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;
}
