package com.autoblog.application;

import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class VinNormalizer {

    private static final Pattern ALLOWED_VIN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    public String normalizeAndValidate(String rawVin) {
        String normalized = normalize(rawVin);

        if (normalized.length() != 17) {
            throw new InvalidVinException("VIN must be exactly 17 characters");
        }
        if (!ALLOWED_VIN.matcher(normalized).matches()) {
            throw new InvalidVinException("VIN may contain only A-Z and 0-9, excluding I, O, and Q");
        }

        return normalized;
    }

    public String normalize(String rawVin) {
        if (rawVin == null) {
            throw new InvalidVinException("VIN is required");
        }

        return rawVin.trim().toUpperCase(Locale.ROOT);
    }
}
