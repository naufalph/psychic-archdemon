package com.rumantra.university.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.university.domain.University;
import com.rumantra.university.dto.UniversityResponse;
import com.rumantra.university.repository.UniversityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UniversityService {

  private final UniversityRepository universityRepository;

  @Transactional(readOnly = true)
  public List<UniversityResponse> list(String search) {
    List<University> universities =
        (search == null || search.isBlank())
            ? universityRepository.findAllByOrderBySortOrderAsc()
            : universityRepository.findAllByNameContainingIgnoreCaseOrderBySortOrderAsc(
                search.trim());

    return universities.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  private UniversityResponse mapToResponse(University university) {
    return UniversityResponse.builder()
        .id(university.getId())
        .name(university.getName())
        .country(university.getCountry())
        .city(university.getCity())
        .indonesia(university.isIndonesia())
        .qsRank(university.getQsRank())
        .build();
  }
}
