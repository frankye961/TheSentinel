package com.sentinel.service;

import com.sentinel.config.SecurityCheckProperties;
import com.sentinel.model.CheckResult;
import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.OutcomeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityChecksServiceTests {

    private final SecurityChecksService check = new SecurityChecksService(
            new SecurityCheckProperties(List.of("X-Content-Type-Options")));

    @Test
    void reportsAMissingHeader() {
        CheckResult result = check.check(snapshot(new HttpHeaders())).getFirst();

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

        CheckResult result = check.check(snapshot(headers)).getFirst();

        assertThat(result.outcome()).isEqualTo(OutcomeEnum.PASS);
        assertThat(result.evidence()).containsIgnoringCase("nosniff");
    }

    @ParameterizedTest
    @ValueSource(strings = {"sniff", "", "unexpected, nosniff"})
    void reportsAnUnexpectedFirstValue(String value) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content-Type-Options", value);

        CheckResult result = check.check(snapshot(headers)).getFirst();

        assertThat(result.outcome()).isEqualTo(OutcomeEnum.FINDING);
        assertThat(result.evidence()).contains("X-Content-Type-Options");
        assertThat(result.recommendation()).contains("nosniff");
    }

    @Test
    void hstsDoesNotApplyToHttpTargets() {
        SecurityChecksService hsts = hstsCheck();
        HttpResponseSnapshot response = new HttpResponseSnapshot(
                URI.create("http://localhost:8081/lab"), new HttpHeaders(), HttpStatus.OK);

        assertThat(hsts.check(response).getFirst().outcome()).isEqualTo(OutcomeEnum.NOT_APPLICABLE);
    }

    @Test
    void reportsMissingHstsOnHttps() {
        assertThat(hstsCheck().check(snapshot(new HttpHeaders())).getFirst().outcome())
                .isEqualTo(OutcomeEnum.FINDING);
    }

    @ParameterizedTest
    @ValueSource(strings = {"max-age=31536000", "max-age=\"60\"; includeSubDomains"})
    void acceptsPositiveHstsMaxAge(String value) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Strict-Transport-Security", value);

        assertThat(hstsCheck().check(snapshot(headers)).getFirst().outcome()).isEqualTo(OutcomeEnum.PASS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"max-age=0", "max-age=invalid", "includeSubDomains", "max-age=60; max-age=120"})
    void reportsHstsWithoutOnePositiveMaxAge(String value) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Strict-Transport-Security", value);

        assertThat(hstsCheck().check(snapshot(headers)).getFirst().outcome()).isEqualTo(OutcomeEnum.FINDING);
    }

    private static SecurityChecksService hstsCheck() {
        return new SecurityChecksService(new SecurityCheckProperties(List.of("Strict-Transport-Security")));
    }

    private static HttpResponseSnapshot snapshot(HttpHeaders headers) {
        return new HttpResponseSnapshot(URI.create("https://localhost:8443/lab"), headers, HttpStatus.OK);
    }
}
