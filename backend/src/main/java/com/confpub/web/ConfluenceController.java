package com.confpub.web;

import com.confpub.domain.PublishLog;
import com.confpub.service.PublishingService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/confluence")
@Validated
public class ConfluenceController {

    private final PublishingService publishingService;

    public ConfluenceController(PublishingService publishingService) {
        this.publishingService = publishingService;
    }

    @PostMapping("/publish")
    public ResponseEntity<?> publishPage(@RequestParam @NotNull Long pageId) {
        try {
            PublishLog log = publishingService.publishNow(pageId);
            return ResponseEntity.ok(Map.of(
                    "pageId", log.getPage().getId(),
                    "remotePageId", log.getRemotePageId(),
                    "status", log.getStatus().name(),
                    "provider", log.getProvider(),
                    "message", log.getMessage()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/status/{pageId}")
    public ResponseEntity<?> getStatus(@PathVariable Long pageId) {
        PublishLog latest = publishingService.getLatestLogForPage(pageId);
        if (latest == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of(
                "pageId", latest.getPage().getId(),
                "remotePageId", latest.getRemotePageId(),
                "status", latest.getStatus().name(),
                "provider", latest.getProvider(),
                "message", latest.getMessage(),
                "createdAt", latest.getCreatedAt()
        ));
    }
}

