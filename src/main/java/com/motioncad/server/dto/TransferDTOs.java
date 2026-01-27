package com.motioncad.server.dto;

public class TransferDTOs {

        public record UploadResponse(
                        String fileId,
                        String url) {
        }

        public record LatestFileResponse(
                        String fileId) {
        }

        public record FileResponse(
                        byte[] content,
                        String fileName) {
        }
}
