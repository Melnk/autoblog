package com.autoblog.attachment.config;

import com.autoblog.attachment.application.AttachmentStorage;
import com.autoblog.attachment.application.StorageProperties;
import com.autoblog.attachment.infrastructure.FileSystemAttachmentStorage;
import com.autoblog.attachment.infrastructure.S3AttachmentStorage;
import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration(proxyBeanMethods = false)
public class AttachmentStorageConfiguration {

    @Bean
    @ConditionalOnProperty(
            prefix = "autoblog.storage",
            name = "type",
            havingValue = "local",
            matchIfMissing = true
    )
    AttachmentStorage fileSystemAttachmentStorage(StorageProperties properties) {
        return new FileSystemAttachmentStorage(properties);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "autoblog.storage", name = "type", havingValue = "s3")
    S3Client attachmentS3Client(StorageProperties properties) {
        StorageProperties.S3 s3 = properties.getS3();
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(s3.getRegion()))
                .forcePathStyle(s3.isPathStyleAccess());
        if (s3.getEndpoint() != null && !s3.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(s3.getEndpoint()));
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "autoblog.storage", name = "type", havingValue = "s3")
    AttachmentStorage s3AttachmentStorage(S3Client attachmentS3Client, StorageProperties properties) {
        String bucket = properties.getS3().getBucket();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("autoblog.storage.s3.bucket must be configured for S3 storage");
        }
        return new S3AttachmentStorage(attachmentS3Client, bucket);
    }
}
