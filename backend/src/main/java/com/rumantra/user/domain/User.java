package com.rumantra.user.domain;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.rumantra.architect.domain.Architect;
import com.rumantra.client.domain.Client;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "rmtr_user")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "password_hash", nullable = false)
  private String password;

  @Column(name = "email", nullable = false)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "social_type", nullable = false)
  @Builder.Default()
  private SocialType socialType = SocialType.EMAIL;

  @Column(name = "first_nm")
  private String firstName;

  @Column(name = "last_nm")
  private String lastName;

  @Column(name = "is_email_verified")
  @Builder.Default()
  private boolean isEmailVerified = false;

  @Column(name = "is_active")
  @Builder.Default()
  private boolean isActive = true;

  @Column(name = "created_at", nullable = false)
  @Builder.Default()
  private Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());

  @Column(name = "updated_at")
  private Timestamp updatedAt;

  // Role relationships - eagerly fetched for security checks
  @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
  private Architect architect;

  @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
  private Client client;
}
