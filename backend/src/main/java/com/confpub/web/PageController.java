package com.confpub.web;

import com.confpub.domain.Page;
import com.confpub.repository.PageRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
@Validated
public class PageController {

    private final PageRepository pageRepository;

    public PageController(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @GetMapping
    public List<Page> listPages() {
        return pageRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Page> getPage(@PathVariable Long id) {
        return pageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Page> createPage(
            @RequestParam @NotBlank String title,
            @RequestParam @NotBlank String content,
            @RequestParam @NotBlank String spaceKey,
            @RequestParam(required = false) String parentPageId
    ) {
        Page page = new Page();
        page.setTitle(title);
        page.setContent(content);
        page.setSpaceKey(spaceKey);
        page.setParentPageId(parentPageId);
        Page saved = pageRepository.save(page);
        return ResponseEntity.ok(saved);
    }
}

