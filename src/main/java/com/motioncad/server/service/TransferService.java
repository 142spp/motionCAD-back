package com.motioncad.server.service;

import com.motioncad.server.domain.TransferFile;
import com.motioncad.server.dto.TransferDTOs;
import com.motioncad.server.repository.TransferFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final TransferFileRepository transferFileRepository;
    private final S3Service s3Service;

    @Transactional
    public TransferDTOs.UploadResponse uploadGlbFile(MultipartFile glbFile, String sessionId) throws IOException {
        log.info("Uploading GLB file: {} (size: {} bytes)", glbFile.getOriginalFilename(), glbFile.getSize());

        // Upload to S3
        String s3Key = s3Service.uploadFile(glbFile, "transfers");

        // Create TransferFile entity
        TransferFile transferFile = TransferFile.builder()
                .sessionId(sessionId)
                .s3Key(s3Key)
                .originalFileName(glbFile.getOriginalFilename())
                .fileSize(glbFile.getSize())
                .build();

        transferFile = transferFileRepository.save(transferFile);

        // Generate presigned URL for download
        String presignedUrl = s3Service.generatePresignedUrl(s3Key);

        log.info("GLB file uploaded successfully. FileId: {}, S3Key: {}", transferFile.getFileId(), s3Key);

        return new TransferDTOs.UploadResponse(
                transferFile.getFileId(),
                presignedUrl);
    }

    @Transactional(readOnly = true)
    public Optional<String> getFileDownloadUrl(String fileId) {
        log.info("Getting download URL for fileId: {}", fileId);

        return transferFileRepository.findByFileId(fileId)
                .map(transferFile -> {
                    String url = s3Service.generatePresignedUrl(transferFile.getS3Key());
                    log.info("Generated download URL for fileId: {}", fileId);
                    return url;
                });
    }

    @Transactional(readOnly = true)
    public Optional<TransferDTOs.FileResponse> downloadFile(String fileId) {
        log.info("Downloading file with fileId: {}", fileId);

        return transferFileRepository.findByFileId(fileId)
                .map(transferFile -> {
                    byte[] content = s3Service.downloadFile(transferFile.getS3Key());
                    log.info("Successfully fetched file content from S3 for fileId: {}", fileId);
                    return new TransferDTOs.FileResponse(content, transferFile.getOriginalFileName());
                });
    }

    @Transactional(readOnly = true)
    public Optional<TransferDTOs.LatestFileResponse> getLatestFile() {
        log.info("Getting latest uploaded file");

        return transferFileRepository.findTopByOrderByCreatedAtDesc()
                .map(transferFile -> {
                    log.info("Latest file found: fileId={}, createdAt={}",
                            transferFile.getFileId(), transferFile.getCreatedAt());
                    return new TransferDTOs.LatestFileResponse(transferFile.getFileId());
                });
    }
}
