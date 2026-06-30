package com.autoblog.application;

import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class VinNormalizer {

    private static final Pattern VALID_VIN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    public String normalizeAndValidate(String rawVin) {
        if (rawVin == null) {
            throw new InvalidVinException("VIN is required");
        }

        String normalized = rawVin
                .replaceAll("[\\s-]", "")
                .toUpperCase(Locale.ROOT);

        if (!VALID_VIN.matcher(normalized).matches()) {
            throw new InvalidVinException("VIN must be 17 characters and cannot contain I, O, or Q");
        }

        return normalized;
    }
}
