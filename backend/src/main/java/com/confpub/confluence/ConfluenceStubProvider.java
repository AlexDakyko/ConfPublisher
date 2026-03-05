package com.confpub.confluence;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConfluenceStubProvider implements PublishingProvider {

    private final Map<String, String> statusByRemoteId = new ConcurrentHashMap<>();

    @Override
    public PublishResult publishPage(String spaceKey,
                                     String title,
                                     String content,
                                     String parentPageId,
                                     List<String> attachmentPaths) {
        String remoteId = UUID.randomUUID().toString();
        String status = "PUBLISHED";
        statusByRemoteId.put(remoteId, status);
        String message = "Stub published page '%s' to space '%s' with %d attachments"
                .formatted(title, spaceKey, attachmentPaths.size());
        return new PublishResult(remoteId, message, status);
    }

    @Override
    public String getStatus(String remotePageId) {
        return statusByRemoteId.getOrDefault(remotePageId, "UNKNOWN");
    }
}

