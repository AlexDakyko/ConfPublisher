package com.confpub.web.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdatePageRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotBlank
    private String spaceKey;

    // Оставляем String, как в CreatePageRequest
    private String parentPageId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSpaceKey() { return spaceKey; }
    public void setSpaceKey(String spaceKey) { this.spaceKey = spaceKey; }
    public String getParentPageId() { return parentPageId; }
    public void setParentPageId(String parentPageId) { this.parentPageId = parentPageId; }
}