package com.motioncad.server.controller;

import com.motioncad.server.service.PartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
@Tag(name = "Part", description = "Asset management APIs")
public class PartController {

    private final PartService partService;

    @GetMapping
    @Operation(summary = "List Parts", description = "Fetches a list of parts filtered by type (BACKGROUND/OBJECT) and sorted by latest or likes.")
    public List<com.motioncad.server.dto.PartResponseDTO> getParts(
            @RequestParam com.motioncad.server.domain.PartType type,
            @RequestParam(defaultValue = "latest") String sort) {
        return partService.getPartsByType(type, sort);
    }

    @PostMapping("/{partId}/like")
    @Operation(summary = "Like Part", description = "Increases the like count of a part.")
    public void likePart(@PathVariable Long partId) {
        partService.addLike(partId);
    }

    @PostMapping("/ai-generate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate Part by AI", description = "Saves an AI generation prompt and returns a dummy GLB model path.")
    public Long generatePart(@RequestParam Long creatorId, @RequestParam String name, @RequestParam String prompt) {
        return partService.createPartByAI(creatorId, name, prompt);
    }
}
