package com.motioncad.server.service;

import com.motioncad.server.domain.Part;
import com.motioncad.server.domain.User;
import com.motioncad.server.repository.PartRepository;
import com.motioncad.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartRepository partRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createPartByAI(Long creatorId, String name, String prompt) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found: " + creatorId));

        Part part = Part.builder()
                .name(name)
                .prompt(prompt)
                .creator(creator)
                .isPublic(true)
                .modelFileUrl("https://assets.motioncad.com/models/placeholder.glb")
                .thumbnailUrl("https://assets.motioncad.com/thumbnails/placeholder.png")
                .category("AI_GENERATED")
                .build();

        return partRepository.save(part).getId();
    }
}
