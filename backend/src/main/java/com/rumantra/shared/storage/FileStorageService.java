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

  String uploadFile(MultipartFile file, String path);

  void deleteImages(java.util.List<String> urls);

  void deleteSingleImage(String url);

  String getImageUrl(String path);

  default String getPublicUrl(String storedPath) {
    return storedPath;
  }

  default byte[] downloadFile(String objectKey) {
    throw new StorageException("Direct download not supported for this storage backend");
  }
}
