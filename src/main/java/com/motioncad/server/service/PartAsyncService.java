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
            long startTime = System.currentTimeMillis();
            log.info("[Performance][Async] Starting async upload for part ID: {}", partId);
            part.setUploadStatus(UploadStatus.PROCESSING);
            partRepository.save(part);

            // Upload model file
            long modelS3StartTime = System.currentTimeMillis();
            String modelS3Key = s3Service.uploadFileBytes(modelData, "models", modelFileName, modelContentType);
            log.info("[Performance][Async] Model S3 upload took {} ms", System.currentTimeMillis() - modelS3StartTime);
            part.setModelFileUrl(modelS3Key);

            // Upload thumbnail file if provided
            if (thumbnailData != null) {
                long thumbS3StartTime = System.currentTimeMillis();
                String thumbnailS3Key = s3Service.uploadFileBytes(thumbnailData, "thumbnails", thumbnailFileName,
                        thumbnailContentType);
                log.info("[Performance][Async] Thumbnail S3 upload took {} ms",
                        System.currentTimeMillis() - thumbS3StartTime);
                part.setThumbnailUrl(thumbnailS3Key);
            }

            part.setUploadStatus(UploadStatus.SUCCESS);
            partRepository.save(part);
            log.info("[Performance][Async] Total async processing took {} ms for part ID: {}",
                    System.currentTimeMillis() - startTime, partId);

        } catch (Exception e) {
            log.error("Failed to upload files for part ID: {}", partId, e);
            part.setUploadStatus(UploadStatus.FAILED);
            partRepository.save(part);
        }
    }
}
