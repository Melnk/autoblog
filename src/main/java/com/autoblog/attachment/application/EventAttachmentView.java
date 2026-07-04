package com.autoblog.attachment.application;

import com.autoblog.attachment.domain.AttachmentType;
import com.autoblog.attachment.domain.AttachmentVisibility;
import java.time.Instant;
import java.util.UUID;

public record EventAttachmentView(
        UUID id,
        UUID vehicleId,
        UUID eventId,
        AttachmentType type,
        AttachmentVisibility visibility,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String checksumSha256,
        String description,
        Instant createdAt
) {
}
