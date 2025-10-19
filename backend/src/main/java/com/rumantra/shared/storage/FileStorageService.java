package com.rumantra.shared.storage;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for file storage operations. Implementations can store files locally or in cloud
 * storage (e.g., Cloudinary, S3).
 */
public interface FileStorageService {

  /**
   * Upload an image and generate multiple sizes.
   *
   * @param file The image file to upload
   * @param architectId The architect ID (for organizing files)
   * @param portoId The portfolio ID (for organizing files)
   * @return Map of ImageSize to URL for each generated image size
   * @throws StorageException if upload fails
   */
  Map<ImageSize, String> uploadImage(MultipartFile file, Long architectId, Long portoId);

  /**
   * Delete images by their URLs.
   *
   * @param urls List of image URLs to delete
   * @throws StorageException if deletion fails
   */
  void deleteImages(java.util.List<String> urls);

  /**
   * Get the public URL for an image path. For local storage, this converts file path to accessible
   * URL. For cloud storage, this might return the path as-is.
   *
   * @param path The file path or identifier
   * @return The public URL to access the image
   */
  String getImageUrl(String path);
}
