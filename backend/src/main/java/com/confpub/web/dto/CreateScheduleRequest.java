package com.confpub.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class CreateScheduleRequest {

    @NotNull
    private Long pageId;

    @NotNull
    @FutureOrPresent
    private Instant scheduledAt;

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
}

