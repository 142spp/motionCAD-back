package com.motioncad.server.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "transfer_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String fileId;

    @Column(length = 100)
    private String sessionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String s3Key;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private Long fileSize;

    @PrePersist
    public void generateFileId() {
        if (this.fileId == null) {
            this.fileId = UUID.randomUUID().toString();
        }
    }
}
