package com.autoblog.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class EventHashService {

    public String hash(EventHashInput input) {
        String canonicalPayload = String.join("\n",
                "vehicleId=" + input.vehicleId(),
                "sequenceNumber=" + input.sequenceNumber(),
                "occurredAt=" + input.occurredAt(),
                "eventType=" + normalize(input.eventType()),
                "description=" + normalize(input.description()),
                "previousHash=" + normalize(input.previousHash())
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(canonicalPayload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value;
    }
}
