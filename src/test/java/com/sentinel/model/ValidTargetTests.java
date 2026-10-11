package com.sentinel.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidTargetTests {

    @ParameterizedTest
    @ValueSource(strings = {"HTTP://localhost:8081/lab", "HtTpS://localhost:8443/lab"})
    void acceptsHttpSchemesRegardlessOfCase(String target) {
        URI uri = URI.create(target);

        assertThat(new ValidTarget(uri, true).absoluteUri()).isEqualTo(uri);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ftp://localhost:8081/lab", "file:///tmp/lab"})
    void rejectsUnsupportedSchemes(String target) {
        assertThatThrownBy(() -> new ValidTarget(URI.create(target), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Scheme must be either 'http' or 'https'");
    }
}
