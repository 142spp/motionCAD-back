package com.motioncad.server.controller;

import com.motioncad.server.service.PartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
@Tag(name = "Part", description = "Asset management APIs")
public class PartController {

    private final PartService partService;

    @PostMapping("/ai-generate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Generate Part by AI", description = "Saves an AI generation prompt and returns a dummy GLB model path.")
    public Long generatePart(@RequestParam Long creatorId, @RequestParam String name, @RequestParam String prompt) {
        return partService.createPartByAI(creatorId, name, prompt);
    }
}
