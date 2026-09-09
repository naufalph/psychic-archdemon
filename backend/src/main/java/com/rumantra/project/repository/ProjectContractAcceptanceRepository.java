package com.rumantra.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.project.domain.ContractParty;
import com.rumantra.project.domain.ProjectContractAcceptance;

@Repository
public interface ProjectContractAcceptanceRepository
    extends JpaRepository<ProjectContractAcceptance, Long> {

  List<ProjectContractAcceptance> findByContractIdOrderByAcceptedAtAsc(Long contractId);

  List<ProjectContractAcceptance> findByProjectIdOrderByAcceptedAtAsc(Long projectId);

  boolean existsByProjectIdAndParty(Long projectId, ContractParty party);
}
