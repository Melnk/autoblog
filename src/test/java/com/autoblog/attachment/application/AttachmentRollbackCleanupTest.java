package com.autoblog.attachment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class AttachmentRollbackCleanupTest {

    @Test
    void deletesStoredObjectWhenDatabaseTransactionRollsBack() {
        AttachmentStorage storage = mock(AttachmentStorage.class);
        var cleanup = new AttachmentRollbackCleanup(storage);
        List<TransactionSynchronization> synchronizations;

        TransactionSynchronizationManager.initSynchronization();
        try {
            cleanup.register("vehicle/event/file.pdf");
            synchronizations = TransactionSynchronizationManager.getSynchronizations();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        assertThat(synchronizations).hasSize(1);
        synchronizations.getFirst().afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        verify(storage).delete("vehicle/event/file.pdf");
    }
}
