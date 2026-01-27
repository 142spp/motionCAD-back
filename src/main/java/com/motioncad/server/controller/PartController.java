package com.motioncad.server.controller;

import com.motioncad.server.service.PartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
@Tag(name = "Part", description = "Asset management APIs")
public class PartController {

    private final PartService partService;

    @GetMapping
    @Operation(summary = "List Parts", description = "Fetches a list of parts filtered by type, category, time range, and AI-generated flag.")
    public List<com.motioncad.server.dto.PartResponseDTO> getParts(
            @RequestParam com.motioncad.server.domain.PartType type,
            @RequestParam(required = false) com.motioncad.server.domain.PartCategory category,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String timeRange,
            @RequestParam(required = false) Boolean isAiGenerated,
            @RequestParam(defaultValue = "20") Integer count) {
        // Validate count parameter: maximum 50
        int validatedCount = Math.min(count != null ? count : 20, 50);
        return partService.getParts(type, category, sort, timeRange, isAiGenerated, validatedCount);
    }

    @GetMapping("/{partId}")
    @Operation(summary = "Get Part by ID", description = "Fetches a single part by its ID.")
    public com.motioncad.server.dto.PartResponseDTO getPart(@PathVariable Long partId) {
        return partService.getPartById(partId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload Part", description = "Uploads a 3D model file with optional thumbnail and registers it as a Part.")
    public Long uploadPart(
            @RequestParam("name") String name,
            @RequestParam("type") com.motioncad.server.domain.PartType type,
            @RequestParam("category") com.motioncad.server.domain.PartCategory category,
            @RequestPart("modelFile") MultipartFile modelFile,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestParam(value = "isAiGenerated", defaultValue = "false") Boolean isAiGenerated) throws IOException {

        // TODO: In production, get userId from SecurityContext
        Long currentUserId = 1L;

        return partService.uploadUserPart(currentUserId, name, type, category, modelFile, thumbnailFile, isAiGenerated);
    }

    @PostMapping("/{partId}/like")
    @Operation(summary = "Like Part", description = "Increases the like count of a part.")
    public void likePart(@PathVariable Long partId) {
        partService.addLike(partId);
    }
}
