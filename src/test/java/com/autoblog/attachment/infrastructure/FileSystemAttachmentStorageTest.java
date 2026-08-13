package com.autoblog.attachment.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.autoblog.attachment.application.StorageProperties;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemAttachmentStorageTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void storesLoadsAndDeletesWithinConfiguredRoot() {
        var properties = new StorageProperties();
        properties.setLocalRoot(temporaryDirectory.toString());
        var storage = new FileSystemAttachmentStorage(properties);

        storage.store("vehicle/event/evidence.pdf", new byte[]{1, 2, 3});

        assertThat(storage.load("vehicle/event/evidence.pdf")).containsExactly(1, 2, 3);
        storage.delete("vehicle/event/evidence.pdf");
        assertThat(temporaryDirectory.resolve("vehicle/event/evidence.pdf")).doesNotExist();
    }

    @Test
    void rejectsKeysThatEscapeConfiguredRoot() {
        var properties = new StorageProperties();
        properties.setLocalRoot(temporaryDirectory.toString());
        var storage = new FileSystemAttachmentStorage(properties);

        assertThatThrownBy(() -> storage.store("../outside.pdf", new byte[]{1}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Storage key is invalid");
    }
}
