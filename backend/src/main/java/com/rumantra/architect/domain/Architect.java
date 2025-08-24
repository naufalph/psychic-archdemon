package com.rumantra.architect.domain;

import com.rumantra.user.domain.User;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "rmtr_architect")
public class Architect {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  @ToString.Exclude
  private User user;

  @Column(name = "company_name", nullable = false, length = 255)
  private String companyName;

  @Column(name = "company_site")
  private String companySite;

  @Column(name = "contact_name", nullable = false, length = 255)
  private String contactName;

  @Column(name = "ktp_num", nullable = false, length = 16)
  private String ktpNum;

  @Column(name = "is_ktp_verified")
  @Builder.Default()
  private boolean ktpVerified = false;

  @Column(name = "npwp", nullable = false, length = 16)
  private String npwp;

  @Column(name = "is_npwp_verified")
  @Builder.Default()
  private boolean npwpVerified = false;

  @Column(name = "bid_left")
  private int bidLeft;

  @Column(name = "success_match")
  @Builder.Default()
  private int successMatch = 0;

  @Column(name = "success_project")
  @Builder.Default()
  private int successProject = 0;
}
