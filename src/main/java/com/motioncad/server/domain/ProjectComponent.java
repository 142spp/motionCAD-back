package com.motioncad.server.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectComponent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private Part part;

    // Position
    private Double posX;
    private Double posY;
    private Double posZ;

    // Rotation
    private Double rotX;
    private Double rotY;
    private Double rotZ;

    // Scale
    private Double scaleX;
    private Double scaleY;
    private Double scaleZ;
}
