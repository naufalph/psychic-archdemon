package com.rumantra.client.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "rmtr_project_file")
public class ProjectFile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  @ToString.Exclude
  private Project project;

  @Column(name = "file_name", nullable = false, length = 255)
  private String fileName;

  @Column(name = "file_path", nullable = false, length = 500)
  private String filePath;

  @Column(name = "file_type", length = 50)
  private String fileType; // png, jpg, pdf

  @Column(name = "file_size")
  private Long fileSize; // in bytes

  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private LocalDateTime uploadedAt;

  @PrePersist
  protected void onCreate() {
    uploadedAt = LocalDateTime.now();
  }
}
