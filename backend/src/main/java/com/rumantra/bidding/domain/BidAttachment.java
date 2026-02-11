package com.rumantra.bidding.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_bid_attachment")
public class BidAttachment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bid_id", nullable = false)
  @ToString.Exclude
  private Bid bid;

  @Enumerated(EnumType.STRING)
  @Column(name = "file_type", nullable = false)
  private AttachmentFileType fileType;

  @Column(name = "file_url", columnDefinition = "TEXT", nullable = false)
  private String fileUrl;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "display_order")
  @Builder.Default
  private Integer displayOrder = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
