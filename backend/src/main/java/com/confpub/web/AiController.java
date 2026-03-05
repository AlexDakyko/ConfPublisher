package com.confpub.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @PostMapping("/improve-content")
    public ResponseEntity<?> improveContent(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "");
        String improved = content.trim().isEmpty()
                ? ""
                : content.trim() + "\n\n[Improved by AI stub: clearer structure and phrasing]";
        return ResponseEntity.ok(Map.of("improvedContent", improved));
    }

    @PostMapping("/generate-summary")
    public ResponseEntity<?> generateSummary(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "");
        String summary = content.length() <= 140
                ? content
                : content.substring(0, 137) + "...";
        return ResponseEntity.ok(Map.of("summary", summary));
    }

    @PostMapping("/suggest-title")
    public ResponseEntity<?> suggestTitle(@RequestBody Map<String, String> body) {
        String content = body.getOrDefault("content", "").trim();
        String base = content.isEmpty()
                ? "Untitled Confluence Page"
                : content.split("\\R", 2)[0];
        String title = base.length() > 60 ? base.substring(0, 57) + "..." : base;

        List<String> suggestions = List.of(
                title,
                "Summary – " + title,
                "Deep Dive – " + title
        );
        return ResponseEntity.ok(Map.of("titles", suggestions));
    }
}

