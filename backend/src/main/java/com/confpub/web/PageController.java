package com.confpub.web;

import com.confpub.domain.Attachment;
import com.confpub.domain.Page;
import com.confpub.domain.PageAttachment;
import com.confpub.domain.PublishLog;
import com.confpub.repository.AttachmentRepository;
import com.confpub.repository.PageAttachmentRepository;
import com.confpub.repository.PageRepository;
import com.confpub.service.PublishingService;
import com.confpub.web.dto.AddAttachmentsRequest;
import com.confpub.web.dto.CreatePageRequest;
import com.confpub.web.dto.PageDetailsResponse;
import com.confpub.web.dto.PublishResponse;
import com.confpub.web.dto.ReorderAttachmentsRequest;
import com.confpub.web.dto.UpdatePageRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pages")
@Validated
public class PageController {

    private final PageRepository pageRepository;
    private final AttachmentRepository attachmentRepository;
    private final PageAttachmentRepository pageAttachmentRepository;
    private final PublishingService publishingService;

    public PageController(PageRepository pageRepository,
                          AttachmentRepository attachmentRepository,
                          PageAttachmentRepository pageAttachmentRepository,
                          PublishingService publishingService) {
        this.pageRepository = pageRepository;
        this.attachmentRepository = attachmentRepository;
        this.pageAttachmentRepository = pageAttachmentRepository;
        this.publishingService = publishingService;
    }

    // ==== Pages ====

    @GetMapping
    public List<Page> listPages() {
        return pageRepository.findAll();
    }

    @GetMapping("/{id}")
    public PageDetailsResponse get(@PathVariable Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Page not found: " + id));
        return toDetailsResponse(page);
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
                Attachment attachment = attachmentRepository.findById(attachmentId).orElse(null);
                if (attachment == null) continue;
                PageAttachment pa = new PageAttachment();
                pa.setPage(saved);
                pa.setAttachment(attachment);
                pa.setPosition(position++);
                pageAttachmentRepository.save(pa);
            }
        }
        return ResponseEntity.ok(toDetailsResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PageDetailsResponse> updatePage(@PathVariable Long id,
                                                          @Valid @RequestBody UpdatePageRequest request) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Page not found: " + id));

        page.setTitle(request.getTitle());
        page.setContent(request.getContent());
        page.setSpaceKey(request.getSpaceKey());
        page.setParentPageId(request.getParentPageId());

        Page saved = pageRepository.save(page);
        return ResponseEntity.ok(toDetailsResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Page not found: " + id));

        // удаляем связи, затем саму страницу
        List<PageAttachment> links = pageAttachmentRepository.findByPageIdOrderByPositionAsc(id);
        pageAttachmentRepository.deleteAll(links);
        pageRepository.delete(page);

        return ResponseEntity.noContent().build();
    }

    // ==== Attachments binding ====

    @PostMapping("/{id}/attachments")
    public ResponseEntity<PageDetailsResponse> addAttachments(@PathVariable Long id,
                                                              @Valid @RequestBody AddAttachmentsRequest request) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Page not found: " + id));

        int position = pageAttachmentRepository.findByPageIdOrderByPositionAsc(id).size();

        for (Long attachmentId : request.getAttachmentIds()) {
            Attachment attachment = attachmentRepository.findById(attachmentId).orElse(null);
            if (attachment == null) continue;
            PageAttachment pa = new PageAttachment();
            pa.setPage(page);
            pa.setAttachment(attachment);
            pa.setPosition(position++);
            pageAttachmentRepository.save(pa);
        }
        return ResponseEntity.ok(toDetailsResponse(page));
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<PageDetailsResponse> removeAttachment(@PathVariable Long id,
                                                                @PathVariable Long attachmentId) {
        List<PageAttachment> links = pageAttachmentRepository.findByPageIdOrderByPositionAsc(id);

        boolean removed = false;
        for (PageAttachment link : links) {
            if (link.getAttachment().getId().equals(attachmentId)) {
                pageAttachmentRepository.delete(link);
                removed = true;
                break;
            }
        }
        if (!removed) {
            throw new NoSuchElementException("Attachment not linked to page: " + attachmentId);
        }

        // переиндексация позиций
        List<PageAttachment> remaining = pageAttachmentRepository.findByPageIdOrderByPositionAsc(id);
        for (int i = 0; i < remaining.size(); i++) {
            if (remaining.get(i).getPosition() != i) {
                remaining.get(i).setPosition(i);
                pageAttachmentRepository.save(remaining.get(i));
            }
        }

        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Page not found: " + id));

        return ResponseEntity.ok(toDetailsResponse(page));
    }

    @PatchMapping("/{id}/attachments/reorder")
    public ResponseEntity<PageDetailsResponse> reorderAttachments(@PathVariable Long id,
                                                                  @Valid @RequestBody ReorderAttachmentsRequest request) {
        List<PageAttachment> links = pageAttachmentRepository.findByPageIdOrderByPositionAsc(id);
        if (links.isEmpty()) {
            throw new NoSuchElementException("No attachments for page: " + id);
        }

        List<Long> ids = request.getAttachmentIdsInOrder();
        if (ids.size() != links.size()) {
            throw new IllegalArgumentException("Attachment count mismatch");
        }

        Map<Long, PageAttachment> byId = links.stream()
                .collect(Collectors.toMap(l -> l.getAttachment().getId(), l -> l));

        int pos = 0;
        for (Long aId : ids) {
            PageAttachment link = byId.get(aId);
            if (link == null) {
                throw new IllegalArgumentException("Unknown attachment id in order: " + aId);
            }
            if (link.getPosition() != pos) {
                link.setPosition(pos);
                pageAttachmentRepository.save(link);
            }
            pos++;
        }

        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Page not found: " + id));

        return ResponseEntity.ok(toDetailsResponse(page));
    }

    // ==== Publishing ====

    @Transactional
    @PostMapping("/{id}/publish")
    public ResponseEntity<PublishResponse> publish(@PathVariable Long id) {
        PublishLog log = publishingService.publishNow(id);
        return ResponseEntity.ok(toPublishResponse(id, log));
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}/publish/status")
    public ResponseEntity<PublishResponse> publishStatus(@PathVariable Long id) {
        PublishLog latest = publishingService.getLatestLogForPage(id);
        if (latest == null) {
            throw new NoSuchElementException("No publish logs for page: " + id);
        }
        return ResponseEntity.ok(toPublishResponse(id, latest));
    }

    // ==== Mappers ====

    private PublishResponse toPublishResponse(Long pageId, PublishLog log) {
        PublishResponse dto = new PublishResponse();
        // Берём pageId из аргумента, чтобы НЕ трогать ленивую связь log.getPage()
        dto.setPageId(pageId);
        dto.setRemotePageId(log.getRemotePageId());
        dto.setStatus(log.getStatus().name());
        dto.setProvider(log.getProvider());
        dto.setMessage(log.getMessage());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
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

        List<PageAttachment> pageAttachments =
                pageAttachmentRepository.findByPageIdOrderByPositionAsc(page.getId());

        List<PageDetailsResponse.AttachmentSummary> attachmentSummaries =
                pageAttachments.stream().map(pa -> {
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
