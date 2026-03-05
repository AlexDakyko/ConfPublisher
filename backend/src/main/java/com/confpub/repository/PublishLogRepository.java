package com.confpub.repository;

import com.confpub.domain.PublishLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublishLogRepository extends JpaRepository<PublishLog, Long> {

    Optional<PublishLog> findFirstByPageIdOrderByCreatedAtDesc(Long pageId);
}

