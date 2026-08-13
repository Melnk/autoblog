package com.autoblog.attachment.infrastructure;

import com.autoblog.attachment.application.AttachmentStorage;
import com.autoblog.attachment.application.StorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemAttachmentStorage implements AttachmentStorage {

    private final Path root;

    public FileSystemAttachmentStorage(StorageProperties properties) {
        this.root = Path.of(properties.getLocalRoot()).toAbsolutePath().normalize();
    }

    @Override
    public void store(String storageKey, byte[] content) {
        Path target = resolve(storageKey);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store attachment", exception);
        }
    }

    @Override
    public byte[] load(String storageKey) {
        Path source = resolve(storageKey);
        try {
            return Files.readAllBytes(source);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load attachment", exception);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to delete attachment", exception);
        }
    }

    private Path resolve(String storageKey) {
        Path resolved = root.resolve(storageKey).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Storage key is invalid");
        }
        return resolved;
    }
}
