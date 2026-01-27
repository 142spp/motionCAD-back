package com.motioncad.server.service;

import com.motioncad.server.domain.Part;
import com.motioncad.server.domain.UploadStatus;
import com.motioncad.server.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartAsyncService {

    private final S3Service s3Service;
    private final PartRepository partRepository;

    @Async("uploadExecutor")
    @Transactional
    public void uploadFilesAsync(Long partId, byte[] modelData, String modelFileName, String modelContentType,
            byte[] thumbnailData, String thumbnailFileName, String thumbnailContentType) {

        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new RuntimeException("Part not found: " + partId));

        try {
            log.info("Starting async upload for part ID: {}", partId);
            part.setUploadStatus(UploadStatus.PROCESSING);
            partRepository.save(part);

            // Upload model file
            String modelS3Key = s3Service.uploadFileBytes(modelData, "models", modelFileName, modelContentType);
            part.setModelFileUrl(modelS3Key);

            // Upload thumbnail file if provided
            if (thumbnailData != null) {
                String thumbnailS3Key = s3Service.uploadFileBytes(thumbnailData, "thumbnails", thumbnailFileName,
                        thumbnailContentType);
                part.setThumbnailUrl(thumbnailS3Key);
            }

            part.setUploadStatus(UploadStatus.SUCCESS);
            partRepository.save(part);
            log.info("Successfully completed async upload for part ID: {}", partId);

        } catch (Exception e) {
            log.error("Failed to upload files for part ID: {}", partId, e);
            part.setUploadStatus(UploadStatus.FAILED);
            partRepository.save(part);
        }
    }
}
