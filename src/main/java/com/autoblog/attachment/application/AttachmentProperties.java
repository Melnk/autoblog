package com.autoblog.attachment.application;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "autoblog.attachments")
public class AttachmentProperties {

    @Min(1)
    private long maxFileSizeMb = 10;

    public long getMaxFileSizeMb() {
        return maxFileSizeMb;
    }

    public void setMaxFileSizeMb(long maxFileSizeMb) {
        this.maxFileSizeMb = maxFileSizeMb;
    }

    public long maxFileSizeBytes() {
        return maxFileSizeMb * 1024L * 1024L;
    }
}
