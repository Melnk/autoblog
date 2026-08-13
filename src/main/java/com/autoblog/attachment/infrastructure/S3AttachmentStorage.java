package com.autoblog.attachment.infrastructure;

import com.autoblog.attachment.application.AttachmentStorage;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class S3AttachmentStorage implements AttachmentStorage {

    private final String bucket;
    private final S3Client s3Client;

    public S3AttachmentStorage(S3Client s3Client, String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public void store(String storageKey, byte[] content) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(storageKey)
                        .build(),
                RequestBody.fromBytes(content)
        );
    }

    @Override
    public byte[] load(String storageKey) {
        return s3Client.getObjectAsBytes(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(storageKey)
                        .build())
                .asByteArray();
    }

    @Override
    public void delete(String storageKey) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build());
    }
}
