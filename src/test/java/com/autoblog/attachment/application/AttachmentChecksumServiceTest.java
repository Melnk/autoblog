package com.autoblog.attachment.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AttachmentChecksumServiceTest {

    private final AttachmentChecksumService checksumService = new AttachmentChecksumService();

    @Test
    void sameFileContentProducesSameSha256Checksum() {
        byte[] content = "receipt-content".getBytes(StandardCharsets.UTF_8);

        String first = checksumService.sha256(content);
        String second = checksumService.sha256(content);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSize(64);
    }
}
