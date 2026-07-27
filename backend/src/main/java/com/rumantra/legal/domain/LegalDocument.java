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
@Table(name = "rmtr_legal_document")
public class LegalDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "doc_type", nullable = false)
  private LegalDocType docType;

  @Column(name = "lang", nullable = false)
  private String lang;

  @Column(name = "version", nullable = false)
  private String version;

  @Column(name = "content_md", nullable = false, columnDefinition = "TEXT")
  private String contentMd;

  @Column(name = "content_hash", nullable = false)
  private String contentHash;

  @Column(name = "effective_at")
  private Timestamp effectiveAt;

  @Column(name = "is_current", nullable = false)
  @Builder.Default
  private boolean isCurrent = false;

  @Column(name = "created_at", nullable = false)
  @Builder.Default
  private Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());
}
