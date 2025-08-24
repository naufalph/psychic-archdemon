package com.rumantra.architect.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.architect.domain.Architect;

@Repository
public interface ArchitectRepository extends JpaRepository<Architect, Long> {

  Optional<Architect> findByUserId(Long userId);

  Optional<Architect> findByUserEmail(String email);

  Optional<Architect> findByUserUserName(String userName);

  boolean existsByUserEmail(String email);

  boolean existsByUserUserName(String userName);

  boolean existsByKtpNum(String ktpNum);

  boolean existsByNpwp(String npwp);
}
