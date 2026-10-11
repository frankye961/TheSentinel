package com.sentinel.service;

import com.sentinel.model.CheckResult;
import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.OutcomeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityChecksServiceTests {

    private final SecurityChecksService check = new SecurityChecksService();

    @Test
    void reportsAMissingHeader() {
        CheckResult result = check.check(snapshot(new HttpHeaders()));

        assertThat(check.checkId()).isEqualTo(result.checkId());
        assertThat(result.checkId()).isEqualTo("X-Content-Type-Options");
        assertThat(result.outcome()).isEqualTo(OutcomeEnum.FINDING);
        assertThat(result.evidence()).contains("missing");
        assertThat(result.recommendation()).contains("nosniff");
    }

    @ParameterizedTest
    @ValueSource(strings = {"nosniff", "NoSnIfF", " nosniff ", "nosniff, unexpected"})
    void acceptsTheFirstNosniffValue(String value) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-content-type-options", value);

        CheckResult result = check.check(snapshot(headers));

        assertThat(result.outcome()).isEqualTo(OutcomeEnum.PASS);
        assertThat(result.evidence()).containsIgnoringCase("nosniff");
    }

    @ParameterizedTest
    @ValueSource(strings = {"sniff", "", "unexpected, nosniff"})
    void reportsAnUnexpectedFirstValue(String value) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content-Type-Options", value);

        CheckResult result = check.check(snapshot(headers));

        assertThat(result.outcome()).isEqualTo(OutcomeEnum.FINDING);
        assertThat(result.evidence()).contains("X-Content-Type-Options");
        assertThat(result.recommendation()).contains("nosniff");
    }

    private static HttpResponseSnapshot snapshot(HttpHeaders headers) {
        return new HttpResponseSnapshot(URI.create("https://localhost:8443/lab"), headers, HttpStatus.OK);
    }
}
