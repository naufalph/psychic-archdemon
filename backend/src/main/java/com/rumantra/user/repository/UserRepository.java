package com.rumantra.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.user.domain.SocialType;
import com.rumantra.user.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndSocialType(String email, SocialType socialType);

  Optional<User> findByEmailAndSocialTypeAndIsActive(
      String email, SocialType socialType, boolean isActive);

  Optional<User> findByEmailAndIsActive(String email, boolean isActive);

  boolean existsByEmail(String email);

  boolean existsByEmailAndSocialType(String email, SocialType socialType);
}
