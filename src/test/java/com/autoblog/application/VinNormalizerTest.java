package com.autoblog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VinNormalizerTest {

    private final VinNormalizer normalizer = new VinNormalizer();

    @Test
    void normalizesVinBeforeValidation() {
        assertThat(normalizer.normalizeAndValidate(" 1hg-cm82633a004352 "))
                .isEqualTo("1HGCM82633A004352");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1HGCM82633A00435",
            "1HGCM82633A0043521",
            "1IGCM82633A004352",
            "1OGCM82633A004352",
            "1QGCM82633A004352",
            "*****************"
    })
    void rejectsInvalidVin(String vin) {
        assertThatThrownBy(() -> normalizer.normalizeAndValidate(vin))
                .isInstanceOf(InvalidVinException.class)
                .hasMessageContaining("VIN must be 17 characters");
    }
}
