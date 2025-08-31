package com.rumantra.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.user.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUserNameAndIsActive(String userName);

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndIsActive(String email);

  boolean existsByEmail(String email);

  Optional<User> findByUserId(Long userId);
}
