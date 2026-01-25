package com.motioncad.server.config;

import com.motioncad.server.domain.Part;
import com.motioncad.server.domain.PartType;
import com.motioncad.server.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final PartRepository partRepository;

  @Override
  public void run(String... args) {
    if (partRepository.count() == 0) {
      List<Part> initialParts = List.of(
          Part.builder()
              .name("Cube")
              .type(PartType.OBJECT)
              .category("Basic")
              .isPublic(true)
              .modelFileUrl("https://assets.motioncad.com/models/cube.glb")
              .thumbnailUrl("https://assets.motioncad.com/thumbnails/cube.png")
              .build(),
          Part.builder()
              .name("Sphere")
              .type(PartType.OBJECT)
              .category("Basic")
              .isPublic(true)
              .modelFileUrl("https://assets.motioncad.com/models/sphere.glb")
              .thumbnailUrl("https://assets.motioncad.com/thumbnails/sphere.png")
              .build(),
          Part.builder()
              .name("Cylinder")
              .type(PartType.OBJECT)
              .category("Basic")
              .isPublic(true)
              .modelFileUrl("https://assets.motioncad.com/models/cylinder.glb")
              .thumbnailUrl("https://assets.motioncad.com/thumbnails/cylinder.png")
              .build(),
          Part.builder()
              .name("Landscape")
              .type(PartType.BACKGROUND)
              .category("Environment")
              .isPublic(true)
              .modelFileUrl("https://assets.motioncad.com/models/landscape.glb")
              .thumbnailUrl("https://assets.motioncad.com/thumbnails/landscape.png")
              .build());
      partRepository.saveAll(initialParts);
    }
  }
}
