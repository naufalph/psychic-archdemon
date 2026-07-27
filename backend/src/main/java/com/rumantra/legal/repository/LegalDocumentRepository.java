package com.rumantra.legal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rumantra.legal.domain.LegalDocType;
import com.rumantra.legal.domain.LegalDocument;

@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {

  Optional<LegalDocument> findByDocTypeAndLangAndIsCurrentTrue(LegalDocType docType, String lang);

  Optional<LegalDocument> findByDocTypeAndLangAndVersion(
      LegalDocType docType, String lang, String version);

  List<LegalDocument> findByDocTypeAndLang(LegalDocType docType, String lang);
}
