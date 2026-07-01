package com.autoblog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VinNormalizerTest {

    private final VinNormalizer normalizer = new VinNormalizer();

    @Test
    void lowercaseVinBecomesUppercase() {
        assertThat(normalizer.normalizeAndValidate("xta217030c0000000"))
                .isEqualTo("XTA217030C0000000");
    }

    @Test
    void spacesAreTrimmed() {
        assertThat(normalizer.normalizeAndValidate(" XTA217030C0000000 "))
                .isEqualTo("XTA217030C0000000");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "XTA217030I0000000",
            "XTA217030O0000000",
            "XTA217030Q0000000"
    })
    void rejectsForbiddenCharacters(String vin) {
        assertThatThrownBy(() -> normalizer.normalizeAndValidate(vin))
                .isInstanceOf(InvalidVinException.class)
                .hasMessageContaining("excluding I, O, and Q");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "XTA217030C000000",
            "XTA217030C00000000"
    })
    void rejectsNonSeventeenLengthVin(String vin) {
        assertThatThrownBy(() -> normalizer.normalizeAndValidate(vin))
                .isInstanceOf(InvalidVinException.class)
                .hasMessageContaining("exactly 17 characters");
    }
}
