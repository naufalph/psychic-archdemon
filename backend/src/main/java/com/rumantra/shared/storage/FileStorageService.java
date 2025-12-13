package com.rumantra.shared.storage;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for file storage operations. Implementations can store files locally or in cloud
 * storage (e.g., Cloudinary, S3).
 */
public interface FileStorageService {

  Map<ImageSize, String> uploadImagePorto(MultipartFile file, Long architectId, Long portoId);

  String uploadImage(MultipartFile file, String path);

  void deleteImages(java.util.List<String> urls);

  void deleteSingleImage(String url);

  /**
   * Get the public URL for an image path. For local storage, this converts file path to accessible
   * URL. For cloud storage, this might return the path as-is.
   *
   * @param path The file path or identifier
   * @return The public URL to access the image
   */
  String getImageUrl(String path);
}
