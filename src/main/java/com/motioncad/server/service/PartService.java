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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createPartByAI(Long creatorId, String name, String prompt) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found: " + creatorId));

        Part part = Part.builder()
                .name(name)
                .prompt(prompt)
                .creator(creator)
                .isPublic(true)
                .isAiGenerated(true)
                .category(PartCategory.ART_ABSTRACT)
                .modelFileUrl("https://assets.motioncad.com/models/placeholder.glb")
                .thumbnailUrl("https://assets.motioncad.com/thumbnails/placeholder.png")
                .build();

        return partRepository.save(part).getId();
    }

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
                .map(PartResponseDTO::from)
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
    public void addLike(Long partId) {
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Part not found: " + partId));
        part.setLikesCount(part.getLikesCount() + 1);
    }

    @Transactional
    public Long createPartWithS3(String name, PartType type, PartCategory category, String modelUrl,
            String thumbnailUrl, String description) {
        Part part = Part.builder()
                .name(name)
                .type(type)
                .category(category)
                .modelFileUrl(modelUrl)
                .thumbnailUrl(thumbnailUrl)
                .description(description)
                .isPublic(true)
                .isAiGenerated(false) // Crawled assets are not AI generated in this context
                .build();

        return partRepository.save(part).getId();
    }
}
