package com.autoblog.attachment.application;

import com.autoblog.attachment.domain.AttachmentType;
import java.util.UUID;

public record PublicAttachmentView(
        UUID id,
        UUID eventId,
        AttachmentType type,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String checksumSha256,
        String description,
        String downloadUrl
) {
}
