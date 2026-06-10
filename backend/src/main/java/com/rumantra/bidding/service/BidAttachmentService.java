package com.rumantra.bidding.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.rumantra.bidding.domain.AttachmentFileType;
import com.rumantra.bidding.domain.Bid;
import com.rumantra.bidding.domain.BidAttachment;
import com.rumantra.bidding.domain.BidStatus;
import com.rumantra.bidding.dto.BidAttachmentResponse;
import com.rumantra.bidding.repository.BidAttachmentRepository;
import com.rumantra.shared.exception.BusinessException;
import com.rumantra.shared.exception.ExceptionConstants;
import com.rumantra.shared.storage.FileStorageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BidAttachmentService {

  private static final long MAX_PDF_SIZE = 20 * 1024 * 1024;
  private static final int MAX_ATTACHMENTS = 1;
  private static final Set<String> ALLOWED_MIME_TYPES = Set.of("application/pdf");

  @Autowired private BidAttachmentRepository attachmentRepository;

  @Autowired private FileStorageService fileStorageService;

  @Transactional
  public BidAttachmentResponse uploadAttachment(Bid bid, MultipartFile file) {
    validateBidStatus(bid);
    validateAttachmentLimit(bid.getId());
    validatePdfFile(file);

    String storagePath = "bids/" + bid.getId() + "/attachments";
    String fileUrl = fileStorageService.uploadFile(file, storagePath);

    BidAttachment attachment =
        BidAttachment.builder()
            .bid(bid)
            .fileType(AttachmentFileType.PDF)
            .fileUrl(fileUrl)
            .fileName(file.getOriginalFilename())
            .fileSize(file.getSize())
            .displayOrder(0)
            .build();

    BidAttachment saved = attachmentRepository.save(attachment);
    log.info(
        "Uploaded attachment for bid ID: {}, filename: {}",
        bid.getId(),
        file.getOriginalFilename());

    return mapToResponse(saved);
  }

  @Transactional
  public void deleteAttachment(Long attachmentId) {
    BidAttachment attachment =
        attachmentRepository
            .findById(attachmentId)
            .orElseThrow(() -> new BusinessException(ExceptionConstants.ATTACHMENT_NOT_FOUND));

    validateBidStatus(attachment.getBid());

    try {
      fileStorageService.deleteSingleImage(attachment.getFileUrl());
    } catch (Exception e) {
      log.warn("Failed to delete file from storage: {}", e.getMessage());
    }

    attachmentRepository.delete(attachment);
    log.info("Deleted attachment ID: {}", attachmentId);
  }

  public List<BidAttachmentResponse> getAttachments(Long bidId) {
    return attachmentRepository.findByBidIdOrderByDisplayOrder(bidId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  private void validateBidStatus(Bid bid) {
    if (bid.getStatus() != BidStatus.DRAFT) {
      throw new BusinessException(ExceptionConstants.ATTACHMENT_MODIFY_NOT_DRAFT);
    }
  }

  private void validateAttachmentLimit(Long bidId) {
    long currentCount = attachmentRepository.countByBidId(bidId);
    if (currentCount >= MAX_ATTACHMENTS) {
      throw new IllegalArgumentException(
          String.format(
              "Attachment limit exceeded. Maximum %d attachment allowed", MAX_ATTACHMENTS));
    }
  }

  private void validatePdfFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new BusinessException(ExceptionConstants.ATTACHMENT_EMPTY_FILE);
    }

    String contentType = file.getContentType();
    if (!ALLOWED_MIME_TYPES.contains(contentType)) {
      throw new BusinessException(ExceptionConstants.ATTACHMENT_INVALID_TYPE);
    }

    if (file.getSize() > MAX_PDF_SIZE) {
      throw new BusinessException(ExceptionConstants.ATTACHMENT_SIZE_EXCEEDED);
    }
  }

  private BidAttachmentResponse mapToResponse(BidAttachment attachment) {
    return BidAttachmentResponse.builder()
        .id(attachment.getId())
        .fileType(attachment.getFileType())
        .fileUrl(fileStorageService.getPublicUrl(attachment.getFileUrl()))
        .fileName(attachment.getFileName())
        .fileSize(attachment.getFileSize())
        .displayOrder(attachment.getDisplayOrder())
        .build();
  }
}
