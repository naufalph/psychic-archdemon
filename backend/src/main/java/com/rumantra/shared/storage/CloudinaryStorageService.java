package com.rumantra.shared.storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Cloudinary implementation of FileStorageService. Uploads files to Cloudinary and uses their
 * transformation URLs.
 *
 * <p>To use this service, set the following in application.yml: file.storage.type=cloudinary
 * file.storage.cloudinary.cloud-name=your_cloud_name file.storage.cloudinary.api-key=your_api_key
 * file.storage.cloudinary.api-secret=your_api_secret
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "cloudinary")
public class CloudinaryStorageService implements FileStorageService {

  private final Cloudinary cloudinary;

  public CloudinaryStorageService(
      @Value("${file.storage.cloudinary.cloud-name}") String cloudName,
      @Value("${file.storage.cloudinary.api-key}") String apiKey,
      @Value("${file.storage.cloudinary.api-secret}") String apiSecret) {

    this.cloudinary =
        new Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));

    log.info("Cloudinary storage initialized with cloud: {}", cloudName);
  }

  @Override
  public Map<ImageSize, String> uploadImage(MultipartFile file, Long architectId, Long portoId) {
    validateImage(file);

    try {
      // Generate unique public ID: portfolios/{architectId}/{portoId}/{uuid}
      String uniqueId = UUID.randomUUID().toString();
      String publicId = "portfolios/" + architectId + "/" + portoId + "/" + uniqueId;

      // Upload original image to Cloudinary
      Map<String, Object> uploadResult =
          cloudinary
              .uploader()
              .upload(
                  file.getBytes(),
                  ObjectUtils.asMap(
                      "public_id", publicId,
                      "folder", "portfolios",
                      "resource_type", "image"));

      String baseUrl = (String) uploadResult.get("secure_url");

      // Generate URLs for different sizes using Cloudinary transformations
      Map<ImageSize, String> urlMap = new HashMap<>();

      for (ImageSize size : ImageSize.values()) {
        String url = generateTransformationUrl(publicId, size);
        urlMap.put(size, url);
      }

      log.info("Uploaded image to Cloudinary: {}", publicId);
      return urlMap;

    } catch (IOException e) {
      throw new StorageException("Failed to upload image to Cloudinary", e);
    }
  }

  @Override
  public void deleteImages(List<String> urls) {
    for (String url : urls) {
      try {
        // Extract public ID from URL
        String publicId = extractPublicIdFromUrl(url);

        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        log.debug("Deleted Cloudinary image: {}", publicId);

      } catch (Exception e) {
        log.error("Failed to delete Cloudinary image: {}", url, e);
        // Continue deleting other files even if one fails
      }
    }
  }

  @Override
  public String getImageUrl(String path) {
    return path; // Cloudinary URLs are already public
  }

  private String generateTransformationUrl(String publicId, ImageSize size) {
    if (size == ImageSize.ORIGINAL) {
      return cloudinary.url().publicId(publicId).generate();
    }

    // Generate transformation URL with width constraint
    return cloudinary
        .url()
        .transformation(
            new Transformation()
                .width(size.getMaxWidth())
                .crop("limit") // Don't upscale smaller images
                .quality("auto") // Auto quality optimization
                .fetchFormat("auto")) // Auto format (WebP for supported browsers)
        .publicId(publicId)
        .generate();
  }

  private String extractPublicIdFromUrl(String url) {
    // Cloudinary URL format:
    // https://res.cloudinary.com/{cloud}/image/upload/{transformations}/{publicId}.{ext}
    // Extract publicId from URL
    String[] parts = url.split("/upload/");
    if (parts.length < 2) {
      throw new StorageException("Invalid Cloudinary URL: " + url);
    }

    String afterUpload = parts[1];
    // Remove transformations and get public ID
    String publicIdWithExt = afterUpload.substring(afterUpload.indexOf("portfolios/"));

    // Remove extension
    int lastDot = publicIdWithExt.lastIndexOf(".");
    return lastDot > 0 ? publicIdWithExt.substring(0, lastDot) : publicIdWithExt;
  }

  private void validateImage(MultipartFile file) {
    if (file.isEmpty()) {
      throw new StorageException("Cannot upload empty file");
    }

    String contentType = file.getContentType();
    if (contentType == null
        || (!contentType.startsWith("image/jpeg")
            && !contentType.startsWith("image/png")
            && !contentType.startsWith("image/jpg"))) {
      throw new StorageException("Only JPEG and PNG images are allowed");
    }

    // Max 10MB
    long maxSize = 10 * 1024 * 1024;
    if (file.getSize() > maxSize) {
      throw new StorageException("File size exceeds maximum allowed size of 10MB");
    }
  }
}
