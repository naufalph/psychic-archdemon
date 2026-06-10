package com.rumantra.shared.storage;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

/**
 * Railway Storage implementation of FileStorageService. Uploads files to Railway's S3-compatible
 * storage with direct public URLs.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "railway")
public class RailwayStorageService implements FileStorageService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucketName;
  private final String baseUrl;

  public RailwayStorageService(
      @Value("${file.storage.railway.bucket-name}") String bucketName,
      @Value("${file.storage.railway.access-key}") String accessKey,
      @Value("${file.storage.railway.secret-key}") String secretKey,
      @Value("${file.storage.railway.region}") String region,
      @Value("${file.storage.railway.endpoint}") String endpoint) {

    this.bucketName = bucketName;
    this.baseUrl = "https://" + bucketName + ".storage.railway.app";

    StaticCredentialsProvider credentials =
        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));

    this.s3Client =
        S3Client.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(credentials)
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .build();

    this.s3Presigner =
        S3Presigner.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(credentials)
            .build();

    log.info("Railway storage initialized with bucket: {}", bucketName);
  }

  @Override
  public String getPublicUrl(String storedPath) {
    String objectKey = extractObjectKeyFromUrl(storedPath);
    if (objectKey == null || objectKey.isEmpty()) {
      return storedPath;
    }
    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofHours(1))
            .getObjectRequest(r -> r.bucket(bucketName).key(objectKey))
            .build();
    return s3Presigner.presignGetObject(presignRequest).url().toString();
  }

  @Override
  public Map<ImageSize, String> uploadImagePorto(
      MultipartFile file, Long architectId, Long portoId) {
    validateImage(file);

    try {
      String extension = getFileExtension(file.getOriginalFilename());
      String uniqueId = UUID.randomUUID().toString();

      BufferedImage originalImage = ImageIO.read(file.getInputStream());
      if (originalImage == null) {
        throw new StorageException("Failed to read image file");
      }

      Map<ImageSize, String> urlMap = new HashMap<>();

      for (ImageSize size : ImageSize.values()) {
        BufferedImage resizedImage = resizeImage(originalImage, size);

        byte[] imageBytes = imageToBytes(resizedImage, extension);

        String objectKey =
            String.format(
                "portfolios/%d/%d/%s_%s.%s",
                architectId, portoId, uniqueId, size.name().toLowerCase(), extension);

        uploadToS3(objectKey, imageBytes, "image/" + extension);

        String url = baseUrl + "/" + objectKey;
        urlMap.put(size, url);

        log.debug("Uploaded {} image to Railway: {}", size, objectKey);
      }

      return urlMap;

    } catch (IOException e) {
      throw new StorageException("Failed to upload image to Railway storage", e);
    }
  }

  @Override
  public String uploadImage(MultipartFile file, String path) {
    validateImage(file);

    try {
      String extension = getFileExtension(file.getOriginalFilename());
      String uniqueId = UUID.randomUUID().toString();
      String objectKey = path + "/" + uniqueId + "." + extension;

      BufferedImage image = ImageIO.read(file.getInputStream());
      if (image == null) {
        throw new StorageException("Failed to read image file");
      }

      byte[] imageBytes = imageToBytes(image, extension);
      uploadToS3(objectKey, imageBytes, "image/" + extension);

      String url = baseUrl + "/" + objectKey;
      log.debug("Uploaded image to Railway: {}", objectKey);

      return url;

    } catch (IOException e) {
      throw new StorageException("Failed to upload image to Railway storage", e);
    }
  }

  @Override
  public String uploadFile(MultipartFile file, String path) {
    validateFile(file);

    try {
      String extension = getFileExtension(file.getOriginalFilename());
      String uniqueId = UUID.randomUUID().toString();
      String objectKey = path + "/" + uniqueId + "." + extension;

      byte[] fileBytes = file.getBytes();
      uploadToS3(objectKey, fileBytes, file.getContentType());

      String url = baseUrl + "/" + objectKey;
      log.debug("Uploaded file to Railway: {}", objectKey);

      return url;

    } catch (IOException e) {
      throw new StorageException("Failed to upload file to Railway storage", e);
    }
  }

  @Override
  public void deleteImages(List<String> urls) {
    if (urls == null || urls.isEmpty()) {
      return;
    }

    List<String> objectKeys =
        urls.stream()
            .map(this::extractObjectKeyFromUrl)
            .filter(key -> key != null && !key.isEmpty())
            .collect(Collectors.toList());

    if (objectKeys.isEmpty()) {
      return;
    }

    // Use batch delete for efficiency (max 1000 objects per request)
    List<List<String>> batches = new ArrayList<>();
    for (int i = 0; i < objectKeys.size(); i += 1000) {
      batches.add(objectKeys.subList(i, Math.min(i + 1000, objectKeys.size())));
    }

    for (List<String> batch : batches) {
      try {
        List<ObjectIdentifier> objectIdentifiers =
            batch.stream().map(key -> ObjectIdentifier.builder().key(key).build()).toList();

        DeleteObjectsRequest deleteRequest =
            DeleteObjectsRequest.builder()
                .bucket(bucketName)
                .delete(Delete.builder().objects(objectIdentifiers).build())
                .build();

        s3Client.deleteObjects(deleteRequest);
        log.debug("Deleted {} images from Railway storage", batch.size());

      } catch (S3Exception e) {
        log.error("Failed to delete batch of images from Railway storage", e);
      }
    }
  }

  @Override
  public void deleteSingleImage(String url) {
    try {
      String objectKey = extractObjectKeyFromUrl(url);
      if (objectKey == null || objectKey.isEmpty()) {
        throw new StorageException("Invalid Railway storage URL: " + url);
      }

      DeleteObjectRequest deleteRequest =
          DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build();

      s3Client.deleteObject(deleteRequest);
      log.debug("Deleted image from Railway storage: {}", objectKey);

    } catch (S3Exception e) {
      if (e.statusCode() == 404) {
        throw new StorageException("Object not found: " + url, e);
      } else if (e.statusCode() == 403) {
        throw new StorageException("Access denied: " + url, e);
      } else {
        throw new StorageException("Failed to delete image from Railway storage", e);
      }
    }
  }

  @Override
  public String getImageUrl(String path) {
    return path;
  }

  private void uploadToS3(String objectKey, byte[] data, String contentType) {
    try {
      PutObjectRequest putRequest =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(objectKey)
              .contentType(contentType)
              .build();

      s3Client.putObject(putRequest, RequestBody.fromBytes(data));

    } catch (S3Exception e) {
      if (e.statusCode() == 413) {
        throw new StorageException("File too large", e);
      } else if (e.statusCode() == 403) {
        throw new StorageException("Access denied to Railway storage", e);
      } else {
        throw new StorageException("Failed to upload to Railway storage", e);
      }
    }
  }

  private String extractObjectKeyFromUrl(String url) {
    if (url == null || !url.contains(baseUrl)) {
      return null;
    }

    return url.replace(baseUrl + "/", "");
  }

  private byte[] imageToBytes(BufferedImage image, String format) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, format, baos);
    return baos.toByteArray();
  }

  private BufferedImage resizeImage(BufferedImage originalImage, ImageSize size) {
    if (size == ImageSize.ORIGINAL) {
      return originalImage;
    }

    int originalWidth = originalImage.getWidth();
    int originalHeight = originalImage.getHeight();
    int targetWidth = size.getMaxWidth();

    if (originalWidth <= targetWidth) {
      return originalImage;
    }

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

    long maxSize = 10 * 1024 * 1024;
    if (file.getSize() > maxSize) {
      throw new StorageException("File size exceeds maximum allowed size of 10MB");
    }
  }

  private void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      log.error("Cannot upload empty file");
      throw new StorageException("Cannot upload empty file");
    }

    long maxSize = 20 * 1024 * 1024;
    if (file.getSize() > maxSize) {
      throw new StorageException("File size exceeds maximum allowed size of 20MB");
    }
  }

  private String getFileExtension(String filename) {
    if (filename == null || !filename.contains(".")) {
      return "jpg";
    }
    return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
  }
}
