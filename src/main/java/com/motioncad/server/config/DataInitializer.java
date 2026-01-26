package com.motioncad.server.config;

import com.motioncad.server.domain.Part;
import com.motioncad.server.domain.PartCategory;
import com.motioncad.server.domain.PartType;
import com.motioncad.server.repository.PartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private final PartRepository partRepository;

	@Override
	public void run(String... args) {
		if (partRepository.count() == 0) {
			List<Part> initialParts = List.of(
					Part.builder()
							.name("Cyberpunk City")
							.type(PartType.BACKGROUND)
							.category(PartCategory.ARCHITECTURE)
							.isPublic(true)
							.isAiGenerated(true)
							.modelFileUrl("https://assets.motioncad.com/models/cyberpunk_city.glb")
							.thumbnailUrl("https://assets.motioncad.com/thumbnails/cyberpunk_city.png")
							.build(),
					Part.builder()
							.name("Golden Retriever")
							.type(PartType.OBJECT)
							.category(PartCategory.ANIMALS_PETS)
							.isPublic(true)
							.modelFileUrl("https://assets.motioncad.com/models/dog.glb")
							.thumbnailUrl("https://assets.motioncad.com/thumbnails/dog.png")
							.build(),
					Part.builder()
							.name("Retro Car")
							.type(PartType.OBJECT)
							.category(PartCategory.CARS_VEHICLES)
							.isPublic(true)
							.isAiGenerated(true)
							.modelFileUrl("https://assets.motioncad.com/models/car.glb")
							.thumbnailUrl("https://assets.motioncad.com/thumbnails/car.png")
							.build());
			partRepository.saveAll(initialParts);
		}
	}
}
