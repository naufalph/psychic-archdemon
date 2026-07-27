package com.rumantra.legal.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.legal.domain.LegalDocType;
import com.rumantra.legal.domain.LegalDocument;
import com.rumantra.legal.domain.UserAgreementAcceptance;
import com.rumantra.legal.dto.AcceptanceRequest;
import com.rumantra.legal.dto.LegalDocumentResponseDto;
import com.rumantra.legal.repository.LegalDocumentRepository;
import com.rumantra.legal.repository.UserAgreementAcceptanceRepository;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgreementService {

  private static final Set<LegalDocType> REQUIRED_DOC_TYPES =
      EnumSet.of(LegalDocType.ACCOUNT_TC, LegalDocType.PRIVACY_POLICY);

  private final LegalDocumentRepository legalDocumentRepository;
  private final UserAgreementAcceptanceRepository acceptanceRepository;

  public LegalDocumentResponseDto getCurrent(LegalDocType docType, String lang) {
    LegalDocument document =
        legalDocumentRepository
            .findByDocTypeAndLangAndIsCurrentTrue(docType, lang)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No current legal document for " + docType + "/" + lang));
    return toResponseDto(document);
  }

  public LegalDocumentResponseDto getVersion(LegalDocType docType, String lang, String version) {
    LegalDocument document =
        legalDocumentRepository
            .findByDocTypeAndLangAndVersion(docType, lang, version)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Legal document not found for " + docType + "/" + lang + "/" + version));
    return toResponseDto(document);
  }

  @Transactional
  public void recordAcceptances(
      Long userId, List<AcceptanceRequest> acceptances, String ipAddress, String userAgent) {
    if (acceptances == null || acceptances.isEmpty()) {
      throw new BusinessException(ExceptionConstants.MISSING_REQUIRED_ACCEPTANCE);
    }

    Set<LegalDocType> submittedTypes = EnumSet.noneOf(LegalDocType.class);
    for (AcceptanceRequest acceptance : acceptances) {
      submittedTypes.add(acceptance.getDocType());
    }
    if (!submittedTypes.containsAll(REQUIRED_DOC_TYPES)) {
      throw new BusinessException(ExceptionConstants.MISSING_REQUIRED_ACCEPTANCE);
    }

    for (AcceptanceRequest acceptance : acceptances) {
      LegalDocument current =
          legalDocumentRepository
              .findByDocTypeAndLangAndIsCurrentTrue(acceptance.getDocType(), acceptance.getLang())
              .orElseThrow(() -> new BusinessException(ExceptionConstants.STALE_TERMS));

      if (!current.getVersion().equals(acceptance.getVersion())
          || !current.getContentHash().equals(acceptance.getContentHash())) {
        throw new BusinessException(ExceptionConstants.STALE_TERMS);
      }

      acceptanceRepository.save(
          UserAgreementAcceptance.builder()
              .userId(userId)
              .docType(acceptance.getDocType())
              .version(acceptance.getVersion())
              .contentHash(acceptance.getContentHash())
              .lang(acceptance.getLang())
              .acceptedAt(Timestamp.valueOf(LocalDateTime.now()))
              .ipAddress(ipAddress)
              .userAgent(userAgent)
              .build());
    }
  }

  private LegalDocumentResponseDto toResponseDto(LegalDocument document) {
    return LegalDocumentResponseDto.builder()
        .docType(document.getDocType())
        .lang(document.getLang())
        .version(document.getVersion())
        .contentMd(document.getContentMd())
        .contentHash(document.getContentHash())
        .effectiveAt(document.getEffectiveAt())
        .build();
  }
}
