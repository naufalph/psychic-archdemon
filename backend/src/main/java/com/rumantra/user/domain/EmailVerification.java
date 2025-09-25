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
@Table(name = "rmtr_email_verification")
public class EmailVerification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "token", nullable = false, unique = true)
  private String token;

  @Column(name = "expiry", nullable = false)
  @Builder.Default
  private Timestamp expiry = Timestamp.valueOf(LocalDateTime.now().plusHours(24));

  @Column(name = "verified")
  @Builder.Default
  private boolean verified = false;

  @Column(name = "created_at")
  @Builder.Default
  private Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());

  public boolean isExpired() {
    return expiry.before(Timestamp.valueOf(LocalDateTime.now()));
  }
}
