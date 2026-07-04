package com.autoblog.attachment.api.dto;

import com.autoblog.attachment.application.PublicAttachmentView;
import com.autoblog.attachment.domain.AttachmentType;
import java.util.UUID;

public record PublicAttachmentResponse(
        UUID id,
        AttachmentType type,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String checksumSha256,
        String description,
        String downloadUrl
) {
    public static PublicAttachmentResponse from(PublicAttachmentView view) {
        return new PublicAttachmentResponse(
                view.id(),
                view.type(),
                view.originalFilename(),
                view.contentType(),
                view.sizeBytes(),
                view.checksumSha256(),
                view.description(),
                view.downloadUrl()
        );
    }
}
