package com.autoblog.attachment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class S3AttachmentStorageTest {

    @Test
    void usesConfiguredBucketForStoreLoadAndDelete() {
        S3Client client = mock(S3Client.class);
        when(client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
        when(client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(
                        GetObjectResponse.builder().build(),
                        new byte[]{4, 5, 6}
                ));
        var storage = new S3AttachmentStorage(client, "attachments");

        storage.store("vehicle/event/file.pdf", new byte[]{1, 2, 3});
        assertThat(storage.load("vehicle/event/file.pdf")).containsExactly(4, 5, 6);
        storage.delete("vehicle/event/file.pdf");

        var putRequest = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(client).putObject(putRequest.capture(), any(RequestBody.class));
        assertThat(putRequest.getValue().bucket()).isEqualTo("attachments");
        assertThat(putRequest.getValue().key()).isEqualTo("vehicle/event/file.pdf");

        verify(client).getObjectAsBytes(GetObjectRequest.builder()
                .bucket("attachments")
                .key("vehicle/event/file.pdf")
                .build());
        verify(client).deleteObject(DeleteObjectRequest.builder()
                .bucket("attachments")
                .key("vehicle/event/file.pdf")
                .build());
    }
}
