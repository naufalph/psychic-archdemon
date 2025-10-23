package com.rumantra.architect.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_porto")
public class Porto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "architect_id", nullable = false)
  @ToString.Exclude
  private Architect architect;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "project_date")
  private LocalDate projectDate;

  @Column(name = "location", length = 255)
  private String location;

  @Column(name = "project_type", length = 100)
  private String projectType;

  @Column(name = "is_built")
  @Builder.Default
  private boolean isBuilt = false;

  @OneToMany(
      mappedBy = "porto",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  @ToString.Exclude
  private List<PortoDetail> details = new ArrayList<>();

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
