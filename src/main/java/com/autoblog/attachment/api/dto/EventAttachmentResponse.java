package com.autoblog.attachment.api.dto;

import com.autoblog.attachment.application.EventAttachmentView;
import com.autoblog.attachment.domain.AttachmentType;
import com.autoblog.attachment.domain.AttachmentVisibility;
import java.time.Instant;
import java.util.UUID;

public record EventAttachmentResponse(
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
    public static EventAttachmentResponse from(EventAttachmentView view) {
        return new EventAttachmentResponse(
                view.id(),
                view.vehicleId(),
                view.eventId(),
                view.type(),
                view.visibility(),
                view.originalFilename(),
                view.contentType(),
                view.sizeBytes(),
                view.checksumSha256(),
                view.description(),
                view.createdAt()
        );
    }
}
