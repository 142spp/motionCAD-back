package com.motioncad.server.controller;

import com.motioncad.server.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "S3 Proxy", description = "Proxy APIs for S3 assets to avoid CORS issues")
public class S3ProxyController {

    private final S3Service s3Service;

    @GetMapping("/models/**")
    @Operation(summary = "Proxy Model File", description = "Proxies GLB model files from S3.")
    public ResponseEntity<byte[]> proxyModel(HttpServletRequest request) {
        String fullUri = request.getRequestURI();
        String queryString = request.getQueryString();
        String path = fullUri.substring("/models/".length());
        
        log.info("=== S3 Proxy Model Request ===");
        log.info("Full URI: {}", fullUri);
        log.info("Query String: {}", queryString);
        log.info("Extracted path: {}", path);
        log.info("S3 Key will be: models/{}", path);
        
        try {
            byte[] content = s3Service.downloadFile("models/" + path);
            log.info("Successfully downloaded file from S3, size: {} bytes", content.length);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(content);
        } catch (Exception e) {
            log.error("Failed to proxy model for path: {}", path, e);
            log.error("Exception type: {}, message: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/thumbnails/**")
    @Operation(summary = "Proxy Thumbnail File", description = "Proxies thumbnail images from S3.")
    public ResponseEntity<byte[]> proxyThumbnail(HttpServletRequest request) {
        String path = request.getRequestURI().substring("/thumbnails/".length());
        log.info("Proxying thumbnail request for path: {}", path);
        
        try {
            byte[] content = s3Service.downloadFile("thumbnails/" + path);
            
            MediaType mediaType = MediaType.IMAGE_PNG;
            if (path.toLowerCase().endsWith(".jpg") || path.toLowerCase().endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            }
            
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(content);
        } catch (Exception e) {
            log.error("Failed to proxy thumbnail for path: {}", path, e);
            return ResponseEntity.notFound().build();
        }
    }
}
