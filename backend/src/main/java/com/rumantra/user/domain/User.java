package com.rumantra.user.domain;

import java.sql.Timestamp;
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
@Table(name = "rmtr_user")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "username", unique = true, nullable = false, length = 255)
  private String userName;

  @Column(name = "password_hash", nullable = false)
  private String password;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

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
}
