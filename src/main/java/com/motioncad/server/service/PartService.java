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

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final PartAsyncService partAsyncService;

    @Transactional(readOnly = true)
    public List<PartResponseDTO> getParts(PartType type, PartCategory category, String sortBy, String timeRange,
            Boolean isAiGenerated, int count) {
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
                .limit(count)
                .map(part -> PartResponseDTO.from(part, s3Service))
                .toList();
    }

    @Transactional(readOnly = true)
    public PartResponseDTO getPartById(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Part not found: " + partId));
        return PartResponseDTO.from(part, s3Service);
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
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Calculate file hash for deduplication
        String fileHash = s3Service.calculateFileHash(modelFile);

        // Check if this file already exists
        if (partRepository.existsByFileHash(fileHash)) {
            Part existingPart = partRepository.findByFileHash(fileHash)
                    .orElseThrow(() -> new RuntimeException("Hash exists but part not found"));
            return existingPart.getId();
        }

        // Prepare bytes for async upload before the request ends
        byte[] modelBytes = modelFile.getBytes();
        byte[] thumbnailBytes = (thumbnailFile != null && !thumbnailFile.isEmpty()) ? thumbnailFile.getBytes() : null;

        // Save Part with PROCESSING status and no URLs yet
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

        // Trigger async upload
        partAsyncService.uploadFilesAsync(
                savedPart.getId(),
                modelBytes, modelFile.getOriginalFilename(), modelFile.getContentType(),
                thumbnailBytes,
                thumbnailFile != null ? thumbnailFile.getOriginalFilename() : null,
                thumbnailFile != null ? thumbnailFile.getContentType() : null);

        return savedPart.getId();
    }

    @Transactional
    public void addLike(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Part not found: " + partId));
        part.setLikesCount(part.getLikesCount() + 1);
    }
}
