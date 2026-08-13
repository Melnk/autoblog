package com.autoblog.attachment.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class AttachmentRollbackCleanup {

    private static final Logger log = LoggerFactory.getLogger(AttachmentRollbackCleanup.class);

    private final AttachmentStorage storage;

    public AttachmentRollbackCleanup(AttachmentStorage storage) {
        this.storage = storage;
    }

    public void register(String storageKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_ROLLED_BACK) {
                    return;
                }
                try {
                    storage.delete(storageKey);
                } catch (RuntimeException exception) {
                    log.error("Failed to remove attachment object after transaction rollback: {}", storageKey, exception);
                }
            }
        });
    }
}
