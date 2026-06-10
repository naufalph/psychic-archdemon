package com.rumantra.shared.storage;

import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rmtr/files")
@RequiredArgsConstructor
public class FileProxyController {

  private final FileStorageService fileStorageService;

  // Portfolio images are publicly visible — marketplace showcase content
  @GetMapping("/portfolios/{*objectKey}")
  public ResponseEntity<byte[]> servePortfolioFile(@PathVariable String objectKey) {
    if (!isSafeKey(objectKey)) return ResponseEntity.badRequest().build();
    byte[] data = fileStorageService.downloadFile("portfolios/" + objectKey);
    return ResponseEntity.ok()
        .contentType(guessContentType(objectKey))
        .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
        .body(data);
  }

  // All other files (deliverables, bid images, chat attachments, project docs) require login.
  // Object keys contain UUIDs but authentication is still required to prevent enumeration.
  // TODO: add per-resource ownership checks once frontend is updated to fetch files with auth
  // headers.
  @GetMapping("/{*objectKey}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<byte[]> servePrivateFile(@PathVariable String objectKey) {
    if (!isSafeKey(objectKey)) return ResponseEntity.badRequest().build();
    byte[] data = fileStorageService.downloadFile(objectKey);
    MediaType mediaType = guessContentType(objectKey);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(mediaType);
    headers.setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate());
    if (mediaType.equals(MediaType.APPLICATION_PDF)
        || mediaType.equals(MediaType.APPLICATION_OCTET_STREAM)) {
      headers.setContentDisposition(
          ContentDisposition.attachment().filename(extractFilename(objectKey)).build());
    }
    return ResponseEntity.ok().headers(headers).body(data);
  }

  private boolean isSafeKey(String key) {
    return key != null
        && !key.contains("..")
        && !key.contains("\\")
        && !key.contains("\0")
        && !key.startsWith("/");
  }

  private MediaType guessContentType(String key) {
    String lower = key.toLowerCase();
    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
    if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
    if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
    if (lower.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
    return MediaType.APPLICATION_OCTET_STREAM;
  }

  private String extractFilename(String objectKey) {
    int slash = objectKey.lastIndexOf('/');
    return slash >= 0 ? objectKey.substring(slash + 1) : objectKey;
  }
}
