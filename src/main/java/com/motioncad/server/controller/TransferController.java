package com.motioncad.server.controller;

import com.motioncad.server.dto.TransferDTOs;
import com.motioncad.server.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
@Tag(name = "Transfer", description = "GLB file transfer APIs for motion gesture control")
public class TransferController {

    private final TransferService transferService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload GLB File", description = "Uploads a GLB file to the transfer service. Optionally accepts a sessionId for session-based file management.")
    public TransferDTOs.UploadResponse uploadGlbFile(
            @RequestPart("glbFile") MultipartFile glbFile,
            @RequestParam(value = "sessionId", required = false) String sessionId) throws IOException {

        return transferService.uploadGlbFile(glbFile, sessionId);
    }

    @GetMapping("/download/{fileId}")
    @Operation(summary = "Download GLB File", description = "Redirects to the presigned S3 URL for downloading the GLB file by fileId.")
    public ResponseEntity<Void> downloadGlbFile(@PathVariable String fileId) {
        return transferService.getFileDownloadUrl(fileId)
                .map(url -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(url))
                        .build())
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/latest")
    @Operation(summary = "Get Latest File", description = "Returns the fileId of the most recently uploaded GLB file.")
    public ResponseEntity<TransferDTOs.LatestFileResponse> getLatestFile() {
        return transferService.getLatestFile()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
