package com.autoblog.attachment.application;

public interface AttachmentStorage {

    void store(String storageKey, byte[] content);

    byte[] load(String storageKey);
}
