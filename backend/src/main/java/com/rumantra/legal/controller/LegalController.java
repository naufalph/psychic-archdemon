package com.rumantra.legal.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rumantra.legal.domain.LegalDocType;
import com.rumantra.legal.dto.LegalDocumentResponseDto;
import com.rumantra.legal.service.AgreementService;
import com.rumantra.shared.dto.ApiResponse;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/legal")
@RequiredArgsConstructor
public class LegalController {

  private static final List<String> ALLOWED_LANGS = List.of("en", "id");

  private final AgreementService agreementService;

  @GetMapping("/current")
  public ResponseEntity<ApiResponse<LegalDocumentResponseDto>> getCurrent(
      @RequestParam("type") String type, @RequestParam("lang") String lang) {
    LegalDocType docType = parseDocType(type);
    String validLang = parseLang(lang);
    return ResponseEntity.ok(ApiResponse.success(agreementService.getCurrent(docType, validLang)));
  }

  @GetMapping("/{type}/{lang}/{version}")
  public ResponseEntity<ApiResponse<LegalDocumentResponseDto>> getVersion(
      @PathVariable("type") String type,
      @PathVariable("lang") String lang,
      @PathVariable("version") String version) {
    LegalDocType docType = parseDocType(type);
    String validLang = parseLang(lang);
    return ResponseEntity.ok(
        ApiResponse.success(agreementService.getVersion(docType, validLang, version)));
  }

  private LegalDocType parseDocType(String type) {
    try {
      return LegalDocType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BusinessException(ExceptionConstants.INVALID_LEGAL_DOC_TYPE);
    }
  }

  private String parseLang(String lang) {
    if (!ALLOWED_LANGS.contains(lang.toLowerCase())) {
      throw new BusinessException(ExceptionConstants.INVALID_LEGAL_LANG);
    }
    return lang.toLowerCase();
  }
}
