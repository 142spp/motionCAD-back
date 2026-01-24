package com.handcraft.puzzle.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health Check", description = "Simple health check API")
@RestController
@RequestMapping("/api")
public class HealthController {

    @Operation(summary = "Health check", description = "Returns OK if the server is running")
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
