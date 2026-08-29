package com.rumantra.university.domain;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rmtr_university")
public class University {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "country", nullable = false, length = 100)
  private String country;

  @Column(name = "city", length = 150)
  private String city;

  @Column(name = "is_indonesia", nullable = false)
  @Builder.Default
  private boolean indonesia = false;

  @Column(name = "qs_rank")
  private Integer qsRank;

  @Column(name = "sort_order", nullable = false)
  @Builder.Default
  private int sortOrder = 0;
}
