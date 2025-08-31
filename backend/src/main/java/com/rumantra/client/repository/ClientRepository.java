package com.rumantra.client.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.client.domain.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
  Optional<Client> findByUserId(Long userId);
}
