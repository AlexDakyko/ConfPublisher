package com.confpub.web.dto;

import java.time.Instant;

public class PageSummaryResponse {
    private Long id;
    private String title;
    private String spaceKey;
    private String parentPageId;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSpaceKey() { return spaceKey; }
    public void setSpaceKey(String spaceKey) { this.spaceKey = spaceKey; }

    public String getParentPageId() { return parentPageId; }
    public void setParentPageId(String parentPageId) { this.parentPageId = parentPageId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}