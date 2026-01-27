package com.motioncad.server.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Part extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "file_hash", unique = true, length = 32)
    private String fileHash; // MD5 hash of the model file

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private PartCategory category;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "model_file_url", columnDefinition = "TEXT")
    private String modelFileUrl;

    @Column(name = "is_public")
    private boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @Column(name = "likes_count")
    private int likesCount;

    @Column(name = "views_count")
    private int viewsCount;

    @Column(name = "comment_count")
    private int commentCount;

    @Column(name = "is_ai_generated")
    private boolean isAiGenerated;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status")
    @Builder.Default
    private UploadStatus uploadStatus = UploadStatus.PENDING;
}
