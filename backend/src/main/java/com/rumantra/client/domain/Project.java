package com.rumantra.client.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
@Data
@Table(name = "rmtr_project")
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  @ToString.Exclude
  private Client client;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "location", nullable = false)
  private String location;

  @Column(name = "budget_total")
  private Long budgetTotal;

  @Column(name = "design_budget_min")
  private Long designBudgetMin;

  @Column(name = "design_budget_max")
  private Long designBudgetMax;

  @Column(name = "project_category", length = 255)
  private String projectCategory;

  @Column(name = "building_function", length = 255)
  private String buildingFunction;

  @Column(name = "estimated_build_area")
  private Integer estimatedBuildArea; // in square meters

  @Column(name = "number_of_floors")
  private Integer numberOfFloors;

  @Column(name = "owns_land")
  private Boolean ownsLand;

  @Column(name = "has_legal_documents")
  private Boolean hasLegalDocuments;

  @Column(name = "scope_of_work", columnDefinition = "TEXT")
  private String scopeOfWork;

  @Type(JsonType.class)
  @Column(name = "deliverables", columnDefinition = "jsonb")
  private List<String> deliverables;

  @Column(name = "design_preferences", columnDefinition = "TEXT")
  private String designPreferences;

  @Column(name = "contact_person", length = 255)
  private String contactPerson;

  @Column(name = "expected_start_date")
  private LocalDate expectedStartDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private ProjectStatus status = ProjectStatus.PENDING_APPROVAL;

  @Column(name = "validation_notes", columnDefinition = "TEXT")
  private String validationNotes;

  @OneToMany(
      mappedBy = "project",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  @ToString.Exclude
  private List<ProjectFile> files = new ArrayList<>();

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
