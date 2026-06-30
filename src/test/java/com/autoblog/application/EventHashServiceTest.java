package com.autoblog.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventHashServiceTest {

    private final EventHashService service = new EventHashService();

    @Test
    void hashIsDeterministicForSameInput() {
        EventHashInput input = new EventHashInput(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                7L,
                Instant.parse("2025-01-02T03:04:05Z"),
                "MAINTENANCE",
                "Oil changed",
                "previous-hash"
        );

        assertThat(service.hash(input)).isEqualTo(service.hash(input));
    }

    @Test
    void hashChangesWhenPreviousHashChanges() {
        EventHashInput first = new EventHashInput(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                7L,
                Instant.parse("2025-01-02T03:04:05Z"),
                "MAINTENANCE",
                "Oil changed",
                "previous-hash"
        );
        EventHashInput second = new EventHashInput(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                7L,
                Instant.parse("2025-01-02T03:04:05Z"),
                "MAINTENANCE",
                "Oil changed",
                "different-previous-hash"
        );

        assertThat(service.hash(first)).isNotEqualTo(service.hash(second));
    }
}
