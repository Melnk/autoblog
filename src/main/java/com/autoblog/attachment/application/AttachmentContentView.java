package com.autoblog.attachment.application;

public record AttachmentContentView(
        String originalFilename,
        String contentType,
        byte[] content
) {
}
