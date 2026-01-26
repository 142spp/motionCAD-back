package com.motioncad.server.controller;

import com.motioncad.server.domain.PartCategory;
import com.motioncad.server.domain.PartType;
import com.motioncad.server.service.PartService;
import com.motioncad.server.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetAggregationController {

    private final S3Service s3Service;
    private final PartService partService;

    @PostMapping("/aggregate")
    public ResponseEntity<Long> aggregateAsset(
            @RequestParam("name") String name,
            @RequestParam("type") String type,
            @RequestParam("category") String category,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "sourceId", required = false) String sourceId,
            @RequestPart("modelFile") MultipartFile modelFile,
            @RequestPart(value = "thumbnailFile", required = false) MultipartFile thumbnailFile) throws IOException {

        String modelUrl = s3Service.uploadFile(modelFile, "models");
        String thumbnailUrl = null;
        if (thumbnailFile != null) {
            thumbnailUrl = s3Service.uploadFile(thumbnailFile, "thumbnails");
        }

        Long partId = partService.createPartWithS3(
                name,
                PartType.valueOf(type.toUpperCase()),
                PartCategory.valueOf(category.toUpperCase()),
                modelUrl,
                thumbnailUrl,
                description,
                sourceId);

        return ResponseEntity.ok(partId);
    }

    @GetMapping("/check-duplicate/{sourceId}")
    public ResponseEntity<Boolean> checkDuplicate(@PathVariable("sourceId") String sourceId) {
        return ResponseEntity.ok(partService.existsBySourceId(sourceId));
    }
}
