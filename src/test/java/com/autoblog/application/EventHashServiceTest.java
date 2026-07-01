package com.autoblog.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventHashServiceTest {

    private final EventHashService service = new EventHashService();

    @Test
    void hashIsDeterministicForSameInput() {
        EventHashInput input = input("{\"oil\":\"5W-40\",\"parts\":[\"oil_filter\"]}", "previous-hash");

        assertThat(service.hash(input)).isEqualTo(service.hash(input));
    }

    @Test
    void hashChangesWhenPayloadChanges() {
        EventHashInput first = input("{\"oil\":\"5W-40\",\"parts\":[\"oil_filter\"]}", "previous-hash");
        EventHashInput second = input("{\"oil\":\"0W-40\",\"parts\":[\"oil_filter\"]}", "previous-hash");

        assertThat(service.hash(first)).isNotEqualTo(service.hash(second));
    }

    @Test
    void hashChangesWhenPreviousHashChanges() {
        EventHashInput first = input("{\"oil\":\"5W-40\",\"parts\":[\"oil_filter\"]}", "previous-hash");
        EventHashInput second = input("{\"oil\":\"5W-40\",\"parts\":[\"oil_filter\"]}", "different-previous-hash");

        assertThat(service.hash(first)).isNotEqualTo(service.hash(second));
    }

    private EventHashInput input(String payload, String previousHash) {
        return new EventHashInput(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                7L,
                VehicleEventType.MAINTENANCE,
                LocalDate.parse("2026-06-28"),
                180000,
                "Oil change",
                "Oil 5W-40, oil filter",
                new BigDecimal("4500.00"),
                "RUB",
                "Garage service",
                payload,
                previousHash
        );
    }
}
