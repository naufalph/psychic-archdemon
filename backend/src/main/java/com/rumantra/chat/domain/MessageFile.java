package com.rumantra.chat.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rmtr_message_file")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "message_id", nullable = false)
  private Long messageId;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
  private String fileUrl;

  @Column(name = "file_type")
  private String fileType;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
