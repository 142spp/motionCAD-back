package com.motioncad.server.controller;

import com.motioncad.server.service.PartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Tag(name = "AI Asset Confirmation", description = "Endpoints for confirming and saving AI-generated assets")
public class AiAssetController {

    private final PartService partService;

    @PostMapping("/confirm")
    @Operation(summary = "Confirm AI Asset", description = "Transfers an external AI-generated asset URL to our S3 and registers it as a Part")
    public ResponseEntity<Long> confirmAiAsset(
            @RequestParam("name") String name,
            @RequestParam("externalUrl") String externalUrl) throws IOException {

        // TODO: In production, get userId from SecurityContext (Authentication)
        // For development, we'll use a hardcoded userId or allow it as a parameter
        Long currentUserId = 1L;

        /*
         * Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         * UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
         * Long currentUserId = principal.getId();
         */

        Long partId = partService.confirmAiAsset(currentUserId, name, externalUrl);
        return ResponseEntity.ok(partId);
    }
}
