package com.confpub.web.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ReorderAttachmentsRequest {
    @NotEmpty
    private List<Long> attachmentIdsInOrder;

    public List<Long> getAttachmentIdsInOrder() { return attachmentIdsInOrder; }
    public void setAttachmentIdsInOrder(List<Long> attachmentIdsInOrder) { this.attachmentIdsInOrder = attachmentIdsInOrder; }
}