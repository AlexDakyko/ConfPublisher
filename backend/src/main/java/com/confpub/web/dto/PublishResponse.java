package com.confpub.web.dto;

import java.time.Instant;

public class PublishResponse {
    private Long pageId;
    private String remotePageId;
    private String status;   // SUCCESS | FAILED
    private String provider; // например, ConfluenceStubProvider
    private String message;
    private Instant createdAt;

    public Long getPageId() { return pageId; }
    public void setPageId(Long pageId) { this.pageId = pageId; }
    public String getRemotePageId() { return remotePageId; }
    public void setRemotePageId(String remotePageId) { this.remotePageId = remotePageId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}