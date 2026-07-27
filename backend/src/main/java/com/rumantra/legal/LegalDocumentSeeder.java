package com.rumantra.legal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.rumantra.legal.domain.LegalDocType;
import com.rumantra.legal.domain.LegalDocument;
import com.rumantra.legal.repository.LegalDocumentRepository;
import com.rumantra.shared.HashUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LegalDocumentSeeder implements ApplicationRunner {

  private static final String CLASSPATH_PATTERN = "classpath*:legal/*.md";
  private static final Pattern FILENAME_PATTERN =
      Pattern.compile("^(account-tc|privacy-policy)\\.(en|id)\\.v([0-9]+(?:\\.[0-9]+)*)\\.md$");

  private final LegalDocumentRepository legalDocumentRepository;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources(CLASSPATH_PATTERN);

    int loaded = 0;
    for (Resource resource : resources) {
      String filename = resource.getFilename();
      if (filename == null) {
        continue;
      }
      Matcher matcher = FILENAME_PATTERN.matcher(filename);
      if (!matcher.matches()) {
        log.warn("Skipping unrecognized legal document filename: {}", filename);
        continue;
      }

      LegalDocType docType = toDocType(matcher.group(1));
      String lang = matcher.group(2);
      String version = matcher.group(3);

      String content;
      try (var inputStream = resource.getInputStream()) {
        content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
      } catch (IOException e) {
        log.error("Failed to read legal document file: {}", filename, e);
        continue;
      }

      upsert(docType, lang, version, content);
      loaded++;
    }

    flipCurrentVersions();
    log.info("LegalDocumentSeeder: processed {} legal document file(s)", loaded);
  }

  private void upsert(LegalDocType docType, String lang, String version, String content) {
    String hash = HashUtils.sha256Hex(content);

    legalDocumentRepository
        .findByDocTypeAndLangAndVersion(docType, lang, version)
        .ifPresentOrElse(
            existing -> {
              if (!existing.getContentHash().equals(hash)) {
                log.warn(
                    "Legal document {}/{}/{} content hash changed since it was first seeded - "
                        + "legal doc files are append-only and should never be edited in place. "
                        + "Updating stored hash/content to match the file on disk.",
                    docType,
                    lang,
                    version);
                existing.setContentHash(hash);
                existing.setContentMd(content);
                legalDocumentRepository.save(existing);
              }
            },
            () -> {
              legalDocumentRepository.save(
                  LegalDocument.builder()
                      .docType(docType)
                      .lang(lang)
                      .version(version)
                      .contentMd(content)
                      .contentHash(hash)
                      .isCurrent(false)
                      .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                      .build());
              log.info("Seeded new legal document: {}/{}/{}", docType, lang, version);
            });
  }

  private void flipCurrentVersions() {
    List<LegalDocument> all = legalDocumentRepository.findAll();
    Map<String, List<LegalDocument>> byDocTypeAndLang =
        all.stream().collect(Collectors.groupingBy(doc -> doc.getDocType() + ":" + doc.getLang()));

    for (List<LegalDocument> group : byDocTypeAndLang.values()) {
      LegalDocument highest =
          group.stream()
              .max(
                  Comparator.comparing(
                      LegalDocument::getVersion, LegalDocumentSeeder::compareVersions))
              .orElseThrow();

      for (LegalDocument doc : group) {
        boolean shouldBeCurrent = doc.getId().equals(highest.getId());
        if (doc.isCurrent() != shouldBeCurrent) {
          doc.setCurrent(shouldBeCurrent);
          legalDocumentRepository.save(doc);
        }
      }

      log.info(
          "Current version for {}/{}: v{}",
          highest.getDocType(),
          highest.getLang(),
          highest.getVersion());
    }
  }

  private static LegalDocType toDocType(String slug) {
    return switch (slug) {
      case "account-tc" -> LegalDocType.ACCOUNT_TC;
      case "privacy-policy" -> LegalDocType.PRIVACY_POLICY;
      default -> throw new IllegalArgumentException("Unknown legal document type slug: " + slug);
    };
  }

  static int compareVersions(String left, String right) {
    List<Integer> leftParts = parseVersionParts(left);
    List<Integer> rightParts = parseVersionParts(right);
    int maxLength = Math.max(leftParts.size(), rightParts.size());

    for (int i = 0; i < maxLength; i++) {
      int leftPart = i < leftParts.size() ? leftParts.get(i) : 0;
      int rightPart = i < rightParts.size() ? rightParts.get(i) : 0;
      int comparison = Integer.compare(leftPart, rightPart);
      if (comparison != 0) {
        return comparison;
      }
    }
    return 0;
  }

  private static List<Integer> parseVersionParts(String version) {
    List<Integer> parts = new ArrayList<>();
    for (String part : version.split("\\.")) {
      parts.add(Integer.parseInt(part));
    }
    return parts;
  }
}
