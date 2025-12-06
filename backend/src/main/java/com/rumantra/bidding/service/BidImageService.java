package com.rumantra.bidding.service;

import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidImage;
import com.rumantra.bidding.domain.BidImageType;
import com.rumantra.bidding.dto.BidImageResponse;
import com.rumantra.bidding.repository.BidImageRepository;
import com.rumantra.shared.storage.FileStorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BidImageService {

  @Autowired private BidImageRepository bidImageRepository;

  @Autowired private FileStorageService fileStorageService;

  @Transactional
  public List<BidImageResponse> uploadImages(
      Bid bid, BidImageType imageType, List<MultipartFile> images) {

    if (imageType == BidImageType.CONCEPT_SKETCH) {
      long currentCount = bidImageRepository.countByBidIdAndImageType(bid.getId(), imageType);
      long totalAfterUpload = currentCount + images.size();
      if (totalAfterUpload > 3) {
        throw new IllegalArgumentException(
            "Concept sketches limit exceeded. Current: "
                + currentCount
                + ", Uploading: "
                + images.size()
                + ", Max: 3");
      }
    }

    List<BidImage> savedImages = new ArrayList<>();
    int currentMaxOrder = getMaxDisplayOrder(bid.getId(), imageType);

    for (int i = 0; i < images.size(); i++) {
      MultipartFile file = images.get(i);
      String storagePath = "bids/" + bid.getId() + "/" + imageType.name().toLowerCase();
      String imageUrl = fileStorageService.store(file, storagePath);

      BidImage bidImage =
          BidImage.builder()
              .bid(bid)
              .imageType(imageType)
              .imageUrl(imageUrl)
              .displayOrder(currentMaxOrder + i + 1)
              .fileName(file.getOriginalFilename())
              .fileSize(file.getSize())
              .build();

      savedImages.add(bidImageRepository.save(bidImage));
    }

    return savedImages.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  @Transactional
  public void deleteImage(Long imageId) {
    BidImage image =
        bidImageRepository
            .findById(imageId)
            .orElseThrow(() -> new RuntimeException("Image not found"));

    try {
      fileStorageService.delete(image.getImageUrl());
    } catch (Exception e) {
    }

    bidImageRepository.delete(image);
  }

  public List<BidImageResponse> getImagesByType(Long bidId, BidImageType imageType) {
    return bidImageRepository.findByBidIdAndImageTypeOrderByDisplayOrder(bidId, imageType).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public List<BidImageResponse> getAllImages(Long bidId) {
    return bidImageRepository.findByBidIdOrderByDisplayOrder(bidId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public boolean hasMinimumConceptSketches(Long bidId) {
    return bidImageRepository.countByBidIdAndImageType(bidId, BidImageType.CONCEPT_SKETCH) >= 1;
  }

  private int getMaxDisplayOrder(Long bidId, BidImageType imageType) {
    return bidImageRepository.findByBidIdAndImageTypeOrderByDisplayOrder(bidId, imageType).stream()
        .mapToInt(BidImage::getDisplayOrder)
        .max()
        .orElse(0);
  }

  private BidImageResponse mapToResponse(BidImage image) {
    return BidImageResponse.builder()
        .id(image.getId())
        .imageType(image.getImageType())
        .imageUrl(image.getImageUrl())
        .displayOrder(image.getDisplayOrder())
        .fileName(image.getFileName())
        .fileSize(image.getFileSize())
        .build();
  }
}
