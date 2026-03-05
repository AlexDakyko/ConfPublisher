package com.confpub.repository;

import com.confpub.domain.PageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PageAttachmentRepository extends JpaRepository<PageAttachment, Long> {

    List<PageAttachment> findByPageIdOrderByPositionAsc(Long pageId);
}

