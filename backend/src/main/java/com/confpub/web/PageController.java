package com.confpub.web;

import org.springframework.transaction.annotation.Transactional;
import com.confpub.domain.Attachment;
import com.confpub.domain.Page;
import com.confpub.domain.PageAttachment;
import com.confpub.domain.PublishLog;

import com.confpub.repository.AttachmentRepository;
import com.confpub.repository.PageAttachmentRepository;
import com.confpub.repository.PageRepository;

import com.confpub.service.PublishingService;

import com.confpub.web.dto.CreatePageRequest;
import com.confpub.web.dto.PageDetailsResponse;
import com.confpub.web.dto.UpdatePageRequest;
import com.confpub.web.dto.AddAttachmentsRequest;
import com.confpub.web.dto.ReorderAttachmentsRequest;
import com.confpub.web.dto.PublishResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
    public ResponseEntity<PageDetailsResponse> updatePage(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePageRequest request) {

        return pageRepository.findById(id)
                .map(page -> {
                    page.setTitle(request.getTitle());
                    page.setContent(request.getContent());
                    page.setSpaceKey(request.getSpaceKey());
                    page.setParentPageId(request.getParentPageId());
                    Page saved = pageRepository.save(page);
                    return ResponseEntity.ok(toDetailsResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable Long id) {
        var pageOpt = pageRepository.findById(id);
        if (pageOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var links = pageAttachmentRepository.findByPageIdOrderByPositionAsc(id);
        pageAttachmentRepository.deleteAll(links);

        pageRepository.delete(pageOpt.get());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<PageDetailsResponse> addAttachments(
            @PathVariable Long id,
            @Valid @RequestBody AddAttachmentsRequest request) {

        return pageRepository.findById(id)
                .map(page -> {
                    int position = pageAttachmentRepository
                            .findByPageIdOrderByPositionAsc(id)
                            .size();

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
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/attachments/{attachmentId}")
    public ResponseEntity<PageDetailsResponse> removeAttachment(
            @PathVariable Long id, @PathVariable Long attachmentId) {

        List<PageAttachment> links = pageAttachmentRepository.findByPageIdOrderByPositionAsc(id);
        boolean removed = false;

        for (PageAttachment link : links) {
            if (link.getAttachment().getId().equals(attachmentId)) {
                pageAttachmentRepository.delete(link);
                removed = true;
                break;
            }
        }

        if (!removed) return ResponseEntity.notFound().build();

        List<PageAttachment> remaining = pageAttachmentRepository.findByPageIdOrderByPositionAsc(id);
        for (int i = 0; i < remaining.size(); i++) {
            if (remaining.get(i).getPosition() != i) {
                remaining.get(i).setPosition(i);
                pageAttachmentRepository.save(remaining.get(i));
            }
        }

        return pageRepository.findById(id)
                .map(p -> ResponseEntity.ok(toDetailsResponse(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/attachments/reorder")
    public ResponseEntity<PageDetailsResponse> reorderAttachments(
            @PathVariable Long id,
            @Valid @RequestBody ReorderAttachmentsRequest request) {

        List<PageAttachment> links = pageAttachmentRepository.findByPageIdOrderByPositionAsc(id);
        if (links.isEmpty()) return ResponseEntity.notFound().build();

        List<Long> ids = request.getAttachmentIdsInOrder();
        if (ids.size() != links.size()) return ResponseEntity.badRequest().build();

        Map<Long, PageAttachment> byId = links.stream()
                .collect(Collectors.toMap(l -> l.getAttachment().getId(), l -> l));

        int pos = 0;
        for (Long aId : ids) {
            PageAttachment link = byId.get(aId);
            if (link == null) return ResponseEntity.badRequest().build();

            if (link.getPosition() != pos) {
                link.setPosition(pos);
                pageAttachmentRepository.save(link);
            }
            pos++;
        }

        return pageRepository.findById(id)
                .map(p -> ResponseEntity.ok(toDetailsResponse(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


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
        if (latest == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toPublishResponse(id,latest));
    }

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
                }).collect(Collectors.toList());

        response.setAttachments(attachmentSummaries);
        return response;
    }
}