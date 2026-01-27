package com.motioncad.server.service;

import com.motioncad.server.domain.Part;
import com.motioncad.server.domain.PartCategory;
import com.motioncad.server.domain.PartType;
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
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Calculate file hash for deduplication
        String fileHash = s3Service.calculateFileHash(modelFile);

        // Check if this file already exists
        if (partRepository.existsByFileHash(fileHash)) {
            Part existingPart = partRepository.findByFileHash(fileHash)
                    .orElseThrow(() -> new RuntimeException("Hash exists but part not found"));
            return existingPart.getId(); // Return existing part ID instead of uploading again
        }

        // Upload model file to S3
        String modelS3Key = s3Service.uploadFile(modelFile, "models");

        // Upload thumbnail file to S3 if provided
        String thumbnailS3Key = null;
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            thumbnailS3Key = s3Service.uploadFile(thumbnailFile, "thumbnails");
        }

        Part part = Part.builder()
                .name(name)
                .type(type)
                .category(category)
                .modelFileUrl(modelS3Key)
                .thumbnailUrl(thumbnailS3Key)
                .fileHash(fileHash)
                .creator(creator)
                .isPublic(true)
                .isAiGenerated(isAiGenerated != null ? isAiGenerated : false)
                .build();

        return partRepository.save(part).getId();
    }

    @Transactional
    public void addLike(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Part not found: " + partId));
        part.setLikesCount(part.getLikesCount() + 1);
    }
}
