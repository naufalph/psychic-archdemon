package com.rumantra.bidding.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidImage;
import com.rumantra.bidding.domain.BidImageType;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.dto.BidImageResponse;
import com.rumantra.bidding.repository.BidImageRepository;
import com.rumantra.shared.RumantraConstants;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.shared.storage.FileStorageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BidImageService {

  @Autowired private BidImageRepository bidImageRepository;

  @Autowired private FileStorageService fileStorageService;

  @Transactional
  public List<BidImageResponse> uploadImages(
      Bid bid, BidImageType imageType, List<MultipartFile> images) {

    if (bid.getStatus() != BidStatus.DRAFT) {
      throw new BusinessException(ExceptionConstants.BID_NOT_DRAFT);
    }

    long currentCount = bidImageRepository.countByBidIdAndImageType(bid.getId(), imageType);
    long totalAfterUpload = currentCount + images.size();
    if (totalAfterUpload > RumantraConstants.MAX_BID_IMAGES_PER_TYPE) {
      throw new IllegalArgumentException(
          imageType.name()
              + " image limit exceeded. Current: "
              + currentCount
              + ", Uploading: "
              + images.size()
              + ", Max: "
              + RumantraConstants.MAX_BID_IMAGES_PER_TYPE);
    }

    List<BidImage> savedImages = new ArrayList<>();
    int currentMaxOrder = getMaxDisplayOrder(bid.getId(), imageType);

    for (int i = 0; i < images.size(); i++) {
      MultipartFile file = images.get(i);
      String storagePath = "bids/" + bid.getId() + "/" + imageType.name().toLowerCase();
      String imageUrl = fileStorageService.uploadImage(file, storagePath);

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
            .orElseThrow(() -> new BusinessException(ExceptionConstants.BID_IMAGE_NOT_FOUND));

    if (image.getBid().getStatus() != BidStatus.DRAFT) {
      throw new BusinessException(ExceptionConstants.BID_NOT_DRAFT);
    }

    try {
      fileStorageService.deleteSingleImage(image.getImageUrl());
    } catch (Exception e) {
      log.warn("Failed to delete bid image {} from storage: {}", imageId, e.getMessage());
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
        .imageUrl(fileStorageService.getPublicUrl(image.getImageUrl()))
        .displayOrder(image.getDisplayOrder())
        .fileName(image.getFileName())
        .fileSize(image.getFileSize())
        .build();
  }
}
