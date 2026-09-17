package com.rumantra.project.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import com.rumantra.project.dto.ContractTermsSnapshot;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;

/**
 * The agreement both parties sign before a project leaves NEGOTIATION. Written once and never again
 * -- the database rejects UPDATE and DELETE, so {@code @Immutable} keeps Hibernate from ever
 * flushing one and turning a stray setter call into a trigger exception at runtime.
 */
@Builder
@Getter
@Setter
@Immutable
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_project_contract")
public class ProjectContract {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "project_id", nullable = false)
  private Long projectId;

  @Column(name = "bid_id", nullable = false)
  private Long bidId;

  @Column(name = "template_version", nullable = false, length = 16)
  private String templateVersion;

  @Type(JsonType.class)
  @Column(name = "terms_snapshot", nullable = false, columnDefinition = "jsonb")
  private ContractTermsSnapshot termsSnapshot;

  @Column(name = "body_en", nullable = false, columnDefinition = "TEXT")
  private String bodyEn;

  @Column(name = "body_id", nullable = false, columnDefinition = "TEXT")
  private String bodyId;

  @Column(name = "content_hash", nullable = false, length = 64)
  private String contentHash;

  @Column(name = "generated_at", nullable = false)
  @Builder.Default
  private Timestamp generatedAt = Timestamp.valueOf(LocalDateTime.now());
}
