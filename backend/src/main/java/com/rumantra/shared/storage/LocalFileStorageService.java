package com.rumantra.shared.storage;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * Local file system implementation of FileStorageService. Stores files in the local filesystem and
 * serves them via HTTP.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {

  private final Path uploadPath;
  private final String baseUrl;

  public LocalFileStorageService(
      @Value("${file.storage.local.path:uploads}") String uploadPath,
      @Value("${file.storage.local.base-url:http://localhost:8080/uploads}") String baseUrl) {
    this.uploadPath = Paths.get(uploadPath);
    this.baseUrl = baseUrl;
    initializeStorage();
  }

  private void initializeStorage() {
    try {
      Files.createDirectories(uploadPath);
      log.info("Local file storage initialized at: {}", uploadPath.toAbsolutePath());
    } catch (IOException e) {
      throw new StorageException("Failed to initialize local storage", e);
    }
  }

  @Override
  public Map<ImageSize, String> uploadImagePorto(
      MultipartFile file, Long architectId, Long portoId) {
    validateImage(file);

    try {
      String originalFilename = file.getOriginalFilename();
      String extension = getFileExtension(originalFilename);
      String uniqueId = UUID.randomUUID().toString();

      Path targetDir = uploadPath.resolve(architectId.toString()).resolve(portoId.toString());
      Files.createDirectories(targetDir);

      BufferedImage originalImage = ImageIO.read(file.getInputStream());
      if (originalImage == null) {
        throw new StorageException("Failed to read image file");
      }

      Map<ImageSize, String> urlMap = new HashMap<>();

      for (ImageSize size : ImageSize.values()) {
        String filename = uniqueId + "_" + size.name().toLowerCase() + "." + extension;
        Path filePath = targetDir.resolve(filename);

        BufferedImage resizedImage = resizeImage(originalImage, size);
        ImageIO.write(resizedImage, extension, filePath.toFile());

        String url = baseUrl + "/" + architectId + "/" + portoId + "/" + filename;
        urlMap.put(size, url);

        log.debug("Saved {} image to: {}", size, filePath);
      }

      return urlMap;

    } catch (IOException e) {
      throw new StorageException("Failed to upload image", e);
    }
  }

  @Override
  public String uploadImage(MultipartFile file, String path) {
    validateImage(file);

    try {
      String originalFilename = file.getOriginalFilename();
      String extension = getFileExtension(originalFilename);
      String uniqueId = UUID.randomUUID().toString();
      String filename = uniqueId + "." + extension;

      Path targetDir = uploadPath.resolve(path);
      Files.createDirectories(targetDir);

      Path filePath = targetDir.resolve(filename);
      file.transferTo(filePath.toFile());

      String url = baseUrl + "/" + path + "/" + filename;
      log.debug("Saved image to: {}", filePath);

      return url;

    } catch (IOException e) {
      throw new StorageException("Failed to upload image", e);
    }
  }

  @Override
  public void deleteImages(List<String> urls) {
    for (String url : urls) {
      try {
        String relativePath = url.replace(baseUrl + "/", "");
        Path filePath = uploadPath.resolve(relativePath);

        if (Files.exists(filePath)) {
          Files.delete(filePath);
          log.debug("Deleted file: {}", filePath);
        }
      } catch (IOException e) {
        log.error("Failed to delete file: {}", url, e);
      }
    }
  }

  @Override
  public void deleteSingleImage(String url) {
    try {
      String relativePath = url.replace(baseUrl + "/", "");
      Path filePath = uploadPath.resolve(relativePath);

      if (Files.exists(filePath)) {
        Files.delete(filePath);
        log.debug("Deleted file: {}", filePath);
      } else {
        throw new StorageException("File not found: " + url);
      }
    } catch (IOException e) {
      log.error("Failed to delete file: {}", url, e);
      throw new StorageException("Failed to delete image", e);
    }
  }

  @Override
  public String getImageUrl(String path) {
    return path; // Path is already a URL in local storage
  }

  private void validateImage(MultipartFile file) {
    if (file.isEmpty()) {
      log.error("Cannot upload empty file");
      throw new StorageException("Cannot upload empty file");
    }

    String contentType = file.getContentType();
    if (contentType == null
        || (!contentType.startsWith("image/jpeg")
            && !contentType.startsWith("image/png")
            && !contentType.startsWith("image/jpg"))) {
      log.error("Only JPEG and PNG images are allowed");
      throw new StorageException("Only JPEG and PNG images are allowed");
    }

    // Max 10MB
    long maxSize = 10 * 1024 * 1024;
    if (file.getSize() > maxSize) {
      throw new StorageException("File size exceeds maximum allowed size of 10MB");
    }
  }

  private String getFileExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "jpg";
    }
    return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
  }

  private BufferedImage resizeImage(BufferedImage originalImage, ImageSize size) {
    if (size == ImageSize.ORIGINAL) {
      return originalImage;
    }

    int originalWidth = originalImage.getWidth();
    int originalHeight = originalImage.getHeight();
    int targetWidth = size.getMaxWidth();

    // Don't upscale if image is smaller than target
    if (originalWidth <= targetWidth) {
      return originalImage;
    }

    // Calculate proportional height
    int targetHeight = (int) ((double) originalHeight / originalWidth * targetWidth);

    Image scaledImage =
        originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    BufferedImage resizedImage =
        new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

    Graphics2D g2d = resizedImage.createGraphics();
    g2d.drawImage(scaledImage, 0, 0, null);
    g2d.dispose();

    return resizedImage;
  }
}
