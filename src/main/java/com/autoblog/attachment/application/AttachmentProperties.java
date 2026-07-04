package com.autoblog.attachment.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "autoblog.attachments")
public class AttachmentProperties {

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
