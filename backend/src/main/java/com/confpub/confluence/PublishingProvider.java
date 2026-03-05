package com.confpub.confluence;

import java.util.List;

public interface PublishingProvider {

    record PublishResult(String remotePageId, String message, String status) {
    }

    PublishResult publishPage(
            String spaceKey,
            String title,
            String content,
            String parentPageId,
            List<String> attachmentPaths
    );

    String getStatus(String remotePageId);
}

