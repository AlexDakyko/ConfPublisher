package com.confpub.web.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AddAttachmentsRequest {
    @NotEmpty
    private List<Long> attachmentIds;

    public List<Long> getAttachmentIds() { return attachmentIds; }
    public void setAttachmentIds(List<Long> attachmentIds) { this.attachmentIds = attachmentIds; }
}