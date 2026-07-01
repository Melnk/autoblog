package com.autoblog.application;

import java.math.BigDecimal;
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
                "type=" + input.type(),
                "eventDate=" + input.eventDate(),
                "odometerKm=" + normalize(input.odometerKm()),
                "title=" + normalize(input.title()),
                "description=" + normalize(input.description()),
                "costAmount=" + normalize(input.costAmount()),
                "costCurrency=" + normalize(input.costCurrency()),
                "serviceName=" + normalize(input.serviceName()),
                "payload=" + normalize(input.payloadCanonicalJson()),
                "previousEventHash=" + normalize(input.previousEventHash())
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

    private String normalize(Integer value) {
        return value == null ? "" : value.toString();
    }

    private String normalize(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}
