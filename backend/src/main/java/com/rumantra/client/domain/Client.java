package com.rumantra.client.domain;

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
@Table(name = "rmtr_client")
public class Client {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private User user;

  @Column(name = "phone_num", nullable = false, length = 16)
  private String phoneNumber;

  @Column(name = "is_phonenum_verified")
  @Builder.Default()
  private boolean phoneNumVerified = false; // ?

  @Column(name = "ktp_num", length = 16)
  private String ktpNum;

  @Column(name = "is_ktp_verified")
  @Builder.Default()
  private boolean ktpVerified = false; // ?

  @Column(name = "project_match")
  private int projectMatch;

  @Column(name = "project_finished")
  private int projectFinished;
}
