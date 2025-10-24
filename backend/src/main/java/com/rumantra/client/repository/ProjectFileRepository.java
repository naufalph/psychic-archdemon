package com.rumantra.client.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.client.domain.ProjectFile;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {

  List<ProjectFile> findByProjectId(Long projectId);

  void deleteByProjectId(Long projectId);
}
