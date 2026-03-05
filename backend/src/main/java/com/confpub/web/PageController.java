package com.confpub.web;

import com.confpub.domain.Attachment;
import com.confpub.domain.Page;
import com.confpub.domain.PageAttachment;
import com.confpub.repository.AttachmentRepository;
import com.confpub.repository.PageAttachmentRepository;
import com.confpub.repository.PageRepository;
import com.confpub.web.dto.CreatePageRequest;
import com.confpub.web.dto.PageDetailsResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
@Validated
public class PageController {

    private final PageRepository pageRepository;
    private final AttachmentRepository attachmentRepository;
    private final PageAttachmentRepository pageAttachmentRepository;

    public PageController(PageRepository pageRepository,
                          AttachmentRepository attachmentRepository,
                          PageAttachmentRepository pageAttachmentRepository) {
        this.pageRepository = pageRepository;
        this.attachmentRepository = attachmentRepository;
        this.pageAttachmentRepository = pageAttachmentRepository;
    }

    @GetMapping
    public List<Page> listPages() {
        return pageRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PageDetailsResponse> getPage(@PathVariable Long id) {
        return pageRepository.findById(id)
                .map(this::toDetailsResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PageDetailsResponse> createPage(@Valid @RequestBody CreatePageRequest request) {
        Page page = new Page();
        page.setTitle(request.getTitle());
        page.setContent(request.getContent());
        page.setSpaceKey(request.getSpaceKey());
        page.setParentPageId(request.getParentPageId());

        Page saved = pageRepository.save(page);

        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            int position = 0;
            for (Long attachmentId : request.getAttachmentIds()) {
                Attachment attachment = attachmentRepository.findById(attachmentId)
                        .orElse(null);
                if (attachment == null) {
                    continue;
                }
                PageAttachment pa = new PageAttachment();
                pa.setPage(saved);
                pa.setAttachment(attachment);
                pa.setPosition(position++);
                pageAttachmentRepository.save(pa);
            }
        }

        return ResponseEntity.ok(toDetailsResponse(saved));
    }

    private PageDetailsResponse toDetailsResponse(Page page) {
        PageDetailsResponse response = new PageDetailsResponse();
        response.setId(page.getId());
        response.setTitle(page.getTitle());
        response.setContent(page.getContent());
        response.setSpaceKey(page.getSpaceKey());
        response.setParentPageId(page.getParentPageId());
        response.setCreatedAt(page.getCreatedAt());
        response.setUpdatedAt(page.getUpdatedAt());

        var pageAttachments = pageAttachmentRepository.findByPageIdOrderByPositionAsc(page.getId());
        var attachmentSummaries = pageAttachments.stream().map(pa -> {
            Attachment attachment = pa.getAttachment();
            PageDetailsResponse.AttachmentSummary summary = new PageDetailsResponse.AttachmentSummary();
            summary.setId(attachment.getId());
            summary.setFilename(attachment.getFilename());
            summary.setContentType(attachment.getContentType());
            summary.setSize(attachment.getSize());
            summary.setDescription(attachment.getDescription());
            return summary;
        }).toList();
        response.setAttachments(attachmentSummaries);
        return response;
    }
}

