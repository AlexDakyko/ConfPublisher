package com.confpub.service;

import com.confpub.confluence.PublishingProvider;
import com.confpub.domain.Page;
import com.confpub.domain.PageAttachment;
import com.confpub.domain.PublishLog;
import com.confpub.repository.PageAttachmentRepository;
import com.confpub.repository.PageRepository;
import com.confpub.repository.PublishLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublishingService {

    private final PageRepository pageRepository;
    private final PageAttachmentRepository pageAttachmentRepository;
    private final PublishLogRepository publishLogRepository;
    private final PublishingProvider publishingProvider;

    public PublishingService(PageRepository pageRepository,
                             PageAttachmentRepository pageAttachmentRepository,
                             PublishLogRepository publishLogRepository,
                             PublishingProvider publishingProvider) {
        this.pageRepository = pageRepository;
        this.pageAttachmentRepository = pageAttachmentRepository;
        this.publishLogRepository = publishLogRepository;
        this.publishingProvider = publishingProvider;
    }

    @Transactional
    public PublishLog publishNow(Long pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new IllegalArgumentException("Page not found for id " + pageId));

        List<PageAttachment> pageAttachments = pageAttachmentRepository.findByPageIdOrderByPositionAsc(pageId);
        List<String> attachmentPaths = pageAttachments.stream()
                .map(pa -> pa.getAttachment().getStoragePath())
                .toList();

        PublishingProvider.PublishResult result = publishingProvider.publishPage(
                page.getSpaceKey(),
                page.getTitle(),
                page.getContent(),
                page.getParentPageId(),
                attachmentPaths
        );

        PublishLog log = new PublishLog();
        log.setPage(page);
        log.setProvider(publishingProvider.getClass().getSimpleName());
        log.setRemotePageId(result.remotePageId());
        log.setStatus(PublishLog.Status.SUCCESS);
        log.setMessage(result.message());

        return publishLogRepository.save(log);
    }

    public PublishLog getLatestLogForPage(Long pageId) {
        return publishLogRepository.findFirstByPageIdOrderByCreatedAtDesc(pageId)
                .orElse(null);
    }
}

