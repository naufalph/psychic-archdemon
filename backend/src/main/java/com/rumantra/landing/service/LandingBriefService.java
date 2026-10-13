package com.rumantra.landing.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rumantra.landing.domain.LandingBrief;
import com.rumantra.landing.dto.BriefRequest;
import com.rumantra.landing.dto.BriefResponse;
import com.rumantra.landing.repository.LandingBriefRepository;
import com.rumantra.landing.repository.LandingPresetRepository;
import com.rumantra.shared.constants.ProjectTaxonomy;
import com.rumantra.shared.exception.RateLimitExceededException;
import com.rumantra.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandingBriefService {

  // Generous enough that a shared office NAT will not trip it, tight enough to stop scripted abuse
  private static final int MAX_BRIEFS_PER_IP_PER_HOUR = 30;
  private static final Duration RATE_WINDOW = Duration.ofHours(1);

  // Mirrors SURVEY_FLOW in frontend2/src/constants/projectSurvey.js. furthest_step only moves
  // forward along this order, and the funnel query reads it in the same order.
  private static final List<String> SURVEY_STEPS =
      List.of(
          "lineup",
          "scope",
          "property",
          "akun",
          "alamat",
          "luas",
          "kebutuhan",
          "deliv",
          "fee",
          "mulai",
          "review");

  private final LandingBriefRepository briefRepository;
  private final LandingPresetRepository presetRepository;
  private final SecureRandom secureRandom = new SecureRandom();
  private final Map<String, RateEntry> submissionsByIp = new ConcurrentHashMap<>();

  @Transactional
  public BriefResponse create(BriefRequest request, String clientIp) {
    enforceRateLimit(clientIp);

    LandingBrief brief =
        LandingBrief.builder()
            .claimToken(generateToken())
            .buildingFunction(validCategory(request.getBuildingFunction()))
            .projectScope(validScope(request.getProjectScope()))
            .subCategory(validSubCategory(request.getBuildingFunction(), request.getSubCategory()))
            .title(trimToNull(request.getTitle()))
            .furthestStep(validStep(request.getFurthestStep()))
            .build();

    if (request.getPresetSlug() != null && !request.getPresetSlug().isBlank()) {
      presetRepository.findBySlug(request.getPresetSlug()).ifPresent(brief::setPreset);
    }

    return mapToResponse(briefRepository.save(brief));
  }

  /**
   * Records how far a survey got. The token is the only credential here, so the pre-login answers
   * are overwritten only while the brief is unclaimed; once it belongs to an account those answers
   * live in the project draft and only the progress moves.
   */
  @Transactional
  public BriefResponse updateProgress(String claimToken, BriefRequest request) {
    LandingBrief brief =
        briefRepository
            .findByClaimToken(claimToken)
            .orElseThrow(() -> new ResourceNotFoundException("Brief not found for this token"));

    String step = validStep(request.getFurthestStep());
    if (step != null
        && SURVEY_STEPS.indexOf(step) > SURVEY_STEPS.indexOf(brief.getFurthestStep())) {
      brief.setFurthestStep(step);
    }

    if (brief.getClaimedByUserId() == null) {
      brief.setBuildingFunction(validCategory(request.getBuildingFunction()));
      brief.setProjectScope(validScope(request.getProjectScope()));
      brief.setSubCategory(
          validSubCategory(request.getBuildingFunction(), request.getSubCategory()));
      brief.setTitle(trimToNull(request.getTitle()));
      brief.setPreset(
          request.getPresetSlug() == null || request.getPresetSlug().isBlank()
              ? null
              : presetRepository.findBySlug(request.getPresetSlug()).orElse(null));
    }

    return mapToResponse(briefRepository.save(brief));
  }

  /**
   * Ties a claimed brief to the draft its survey produced, so the funnel can follow a visitor
   * through to submission. Best-effort: a stale or foreign token must never fail the draft.
   */
  @Transactional
  public void linkProject(String claimToken, Long userId, Long projectId) {
    if (claimToken == null || claimToken.isBlank()) {
      return;
    }
    briefRepository
        .findByClaimToken(claimToken)
        .filter(brief -> userId.equals(brief.getClaimedByUserId()) && brief.getProjectId() == null)
        .ifPresent(
            brief -> {
              brief.setProjectId(projectId);
              briefRepository.save(brief);
            });
  }

  /**
   * Claims are single-use: the first authenticated caller takes ownership, and any later request
   * with the same token is rejected so a leaked token cannot be replayed.
   */
  @Transactional
  public BriefResponse claim(String claimToken, Long userId) {
    LandingBrief brief =
        briefRepository
            .findByClaimToken(claimToken)
            .orElseThrow(() -> new ResourceNotFoundException("Brief not found for this token"));

    if (brief.getClaimedByUserId() != null && !brief.getClaimedByUserId().equals(userId)) {
      throw new IllegalStateException("This brief has already been claimed");
    }

    brief.setClaimedByUserId(userId);
    // The survey keeps its token after login to report progress, so it claims again on every
    // visit; the first claim time is the one worth keeping.
    if (brief.getClaimedAt() == null) {
      brief.setClaimedAt(LocalDateTime.now());
    }
    return mapToResponse(briefRepository.save(brief));
  }

  /**
   * Attaches an anonymous brief to a freshly registered account. The verification email cannot
   * carry the claim token, so this is what lets the brief reach the user after they verify on a
   * different device. Best-effort by design: a bad token must never fail a registration.
   */
  @Transactional
  public void linkToUser(String claimToken, Long userId) {
    briefRepository
        .findByClaimToken(claimToken)
        .filter(brief -> brief.getClaimedByUserId() == null)
        .ifPresentOrElse(
            brief -> {
              brief.setClaimedByUserId(userId);
              briefRepository.save(brief);
            },
            () -> log.info("No unlinked landing brief for the supplied token"));
  }

  private void enforceRateLimit(String clientIp) {
    if (clientIp == null || clientIp.isBlank()) {
      return;
    }

    Instant now = Instant.now();
    RateEntry entry =
        submissionsByIp.compute(
            clientIp,
            (ip, existing) -> {
              if (existing == null || now.isAfter(existing.windowStart.plus(RATE_WINDOW))) {
                return new RateEntry(now, 1);
              }
              return new RateEntry(existing.windowStart, existing.count + 1);
            });

    // Opportunistic sweep so the map cannot grow without bound on a long-running instance
    if (submissionsByIp.size() > 10_000) {
      submissionsByIp
          .entrySet()
          .removeIf(e -> now.isAfter(e.getValue().windowStart.plus(RATE_WINDOW)));
    }

    if (entry.count > MAX_BRIEFS_PER_IP_PER_HOUR) {
      log.warn("Landing brief rate limit exceeded for IP {}", clientIp);
      throw new RateLimitExceededException("Too many submissions. Please try again later.");
    }
  }

  private String generateToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  /**
   * A brief is an anonymous draft, so an unrecognised taxonomy value is dropped rather than
   * rejected — losing one dropdown is better than throwing away everything else they typed.
   */
  private String validScope(String scope) {
    String trimmed = trimToNull(scope);
    return ProjectTaxonomy.isValidScope(trimmed) ? trimmed : null;
  }

  private String validCategory(String category) {
    String trimmed = trimToNull(category);
    return ProjectTaxonomy.isValidCategory(trimmed) ? trimmed : null;
  }

  private String validSubCategory(String category, String subCategory) {
    String trimmedCategory = trimToNull(category);
    String trimmed = trimToNull(subCategory);
    return ProjectTaxonomy.isValidSubCategory(trimmedCategory, trimmed) ? trimmed : null;
  }

  private String validStep(String step) {
    String trimmed = trimToNull(step);
    return SURVEY_STEPS.contains(trimmed) ? trimmed : null;
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private BriefResponse mapToResponse(LandingBrief brief) {
    return BriefResponse.builder()
        .claimToken(brief.getClaimToken())
        .buildingFunction(brief.getBuildingFunction())
        .projectScope(brief.getProjectScope())
        .subCategory(brief.getSubCategory())
        .title(brief.getTitle())
        .furthestStep(brief.getFurthestStep())
        .build();
  }

  private record RateEntry(Instant windowStart, int count) {}
}
