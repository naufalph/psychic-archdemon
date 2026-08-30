package com.rumantra.bidding.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.rumantra.bidding.domain.BidImage;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.repository.BidImageRepository;
import com.rumantra.shared.storage.FileStorageService;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reclaims object storage from bids that can no longer be acted on. The stored blob is deleted but
 * the rmtr_bid_image row is kept, stamped with archivedAt, so bid history stays auditable.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BidImageArchiveService {

  private static final List<BidStatus> DEAD_STATUSES =
      List.of(BidStatus.REJECTED, BidStatus.WITHDRAWN);

  private static final int BATCH_SIZE = 200;
  private static final int DRY_RUN_SAMPLE_SIZE = 10000;
  private static final int MAX_BATCHES_PER_RUN = 100;

  private final BidImageRepository bidImageRepository;
  private final FileStorageService fileStorageService;

  @Value("${bid.image.archive.dry-run:true}")
  private boolean dryRun;

  @Value("${bid.image.archive.retention-days:90}")
  private int retentionDays;

  @Getter
  @Builder
  public static class ArchiveResult {
    private final boolean dryRun;
    private final int imageCount;
    private final long totalBytes;
    private final LocalDateTime cutoff;
  }

  public ArchiveResult archiveExpiredImages() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

    if (dryRun) {
      List<BidImage> candidates = findBatch(cutoff, DRY_RUN_SAMPLE_SIZE);
      long bytes = totalBytes(candidates);
      log.info(
          "[DRY RUN] would archive {} bid images / {} MB (bids dead before {})",
          candidates.size(),
          bytes / (1024 * 1024),
          cutoff);
      return ArchiveResult.builder()
          .dryRun(true)
          .imageCount(candidates.size())
          .totalBytes(bytes)
          .cutoff(cutoff)
          .build();
    }

    int archived = 0;
    long bytes = 0;

    for (int batch = 0; batch < MAX_BATCHES_PER_RUN; batch++) {
      List<BidImage> candidates = findBatch(cutoff, BATCH_SIZE);
      if (candidates.isEmpty()) {
        break;
      }

      try {
        archiveBatch(candidates);
      } catch (Exception e) {
        // Abort rather than continue: the next query would return the same unstamped
        // rows and spin. The run simply retries on the next schedule.
        log.error("Aborting bid image archive run after batch failure: {}", e.getMessage(), e);
        break;
      }

      archived += candidates.size();
      bytes += totalBytes(candidates);

      if (candidates.size() < BATCH_SIZE) {
        break;
      }
    }

    log.info(
        "Archived {} bid images / {} MB (bids dead before {})",
        archived,
        bytes / (1024 * 1024),
        cutoff);

    return ArchiveResult.builder()
        .dryRun(false)
        .imageCount(archived)
        .totalBytes(bytes)
        .cutoff(cutoff)
        .build();
  }

  /**
   * Blobs are deleted before the rows are stamped: if storage deletion throws, the rows stay
   * unstamped and the run is retried rather than leaving an orphaned object marked as archived.
   */
  private void archiveBatch(List<BidImage> images) {
    List<String> urls =
        images.stream()
            .map(BidImage::getImageUrl)
            .filter(url -> url != null)
            .collect(Collectors.toList());

    fileStorageService.deleteImages(urls);

    LocalDateTime now = LocalDateTime.now();
    images.forEach(image -> image.setArchivedAt(now));
    bidImageRepository.saveAll(images);
  }

  private List<BidImage> findBatch(LocalDateTime cutoff, int size) {
    return bidImageRepository.findArchivableImages(
        DEAD_STATUSES, BidStatus.DRAFT, cutoff, PageRequest.of(0, size));
  }

  private long totalBytes(List<BidImage> images) {
    return images.stream()
        .mapToLong(image -> image.getFileSize() == null ? 0L : image.getFileSize())
        .sum();
  }
}
