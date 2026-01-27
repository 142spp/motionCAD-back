package com.motioncad.server.service;

import com.motioncad.server.domain.Part;
import com.motioncad.server.domain.PartCategory;
import com.motioncad.server.domain.PartType;
import com.motioncad.server.domain.UploadStatus;
import com.motioncad.server.domain.User;
import com.motioncad.server.dto.PartResponseDTO;
import com.motioncad.server.repository.PartRepository;
import com.motioncad.server.repository.PartSpecification;
import com.motioncad.server.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final PartAsyncService partAsyncService;

    @Transactional(readOnly = true)
    public List<PartResponseDTO> getParts(PartType type, PartCategory category, String sortBy, String timeRange,
            Boolean isAiGenerated) {
        Specification<Part> spec = Specification.where(PartSpecification.hasType(type))
                .and(PartSpecification.isPublic());

        if (category != null) {
            spec = spec.and(PartSpecification.hasCategory(category));
        }

        if (timeRange != null) {
            LocalDateTime start = calculateStartTime(timeRange);
            spec = spec.and(PartSpecification.updatedAfter(start));
        }

        if (isAiGenerated != null) {
            spec = spec.and(PartSpecification.isAiGenerated(isAiGenerated));
        }

        Sort sort = calculateSort(sortBy);
        return partRepository.findAll(spec, sort).stream()
                .map(part -> PartResponseDTO.from(part, s3Service))
                .toList();
    }

    private Sort calculateSort(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "likes" -> Sort.by(Sort.Direction.DESC, "likesCount");
            case "views" -> Sort.by(Sort.Direction.DESC, "viewsCount");
            case "comments" -> Sort.by(Sort.Direction.DESC, "commentCount");
            default -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };
    }

    private LocalDateTime calculateStartTime(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        return switch (timeRange.toLowerCase()) {
            case "day" -> now.minusDays(1);
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            case "year" -> now.minusYears(1);
            default -> null;
        };
    }

    @Transactional
    public Long uploadUserPart(Long userId, String name, PartType type, PartCategory category,
            MultipartFile modelFile, MultipartFile thumbnailFile, Boolean isAiGenerated)
            throws java.io.IOException {
        long startTime = System.currentTimeMillis();
        log.info("[Performance] Starting uploadUserPart for name: {}, size: {} bytes", name, modelFile.getSize());

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Calculate file hash
        long hashStartTime = System.currentTimeMillis();
        String fileHash = s3Service.calculateFileHash(modelFile);
        log.info("[Performance] Hash calculation took {} ms", System.currentTimeMillis() - hashStartTime);

        // Check duplicates
        if (partRepository.existsByFileHash(fileHash)) {
            log.info("[Performance] Duplicate found by hash, returning existing part. Total time: {} ms",
                    System.currentTimeMillis() - startTime);
            Part existingPart = partRepository.findByFileHash(fileHash)
                    .orElseThrow(() -> new RuntimeException("Hash exists but part not found"));
            return existingPart.getId();
        }

        // Read bytes
        long byteReadStartTime = System.currentTimeMillis();
        byte[] modelBytes = modelFile.getBytes();
        byte[] thumbnailBytes = (thumbnailFile != null && !thumbnailFile.isEmpty()) ? thumbnailFile.getBytes() : null;
        log.info("[Performance] Reading bytes into memory took {} ms", System.currentTimeMillis() - byteReadStartTime);

        // Save Part
        long dbSaveStartTime = System.currentTimeMillis();
        Part part = Part.builder()
                .name(name)
                .type(type)
                .category(category)
                .fileHash(fileHash)
                .creator(creator)
                .isPublic(true)
                .isAiGenerated(isAiGenerated != null ? isAiGenerated : false)
                .uploadStatus(UploadStatus.PROCESSING)
                .build();

        Part savedPart = partRepository.save(part);
        log.info("[Performance] DB save (pending) took {} ms", System.currentTimeMillis() - dbSaveStartTime);

        // Trigger async
        partAsyncService.uploadFilesAsync(
                savedPart.getId(),
                modelBytes, modelFile.getOriginalFilename(), modelFile.getContentType(),
                thumbnailBytes,
                thumbnailFile != null ? thumbnailFile.getOriginalFilename() : null,
                thumbnailFile != null ? thumbnailFile.getContentType() : null);

        log.info("[Performance] Total synchronous processing took {} ms. Returning partId: {}",
                System.currentTimeMillis() - startTime, savedPart.getId());
        return savedPart.getId();
    }

    @Transactional
    public void addLike(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Part not found: " + partId));
        part.setLikesCount(part.getLikesCount() + 1);
    }
}
