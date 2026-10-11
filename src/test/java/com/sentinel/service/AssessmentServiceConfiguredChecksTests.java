package com.sentinel.service;

import com.sentinel.checks.SecurityChecks;
import com.sentinel.config.AssessmentProperties;
import com.sentinel.config.SecurityCheckProperties;
import com.sentinel.http.HttpTransport;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssessmentServiceConfiguredChecksTests {

    @Test
    void selectsRegisteredChecksInYamlOrder() {
        SecurityChecks contentTypeCheck = check("X-Content-Type-Options");
        SecurityChecks hstsCheck = check("Strict-Transport-Security");
        AssessmentService service = service(
                List.of("Strict-Transport-Security", "X-Content-Type-Options"),
                List.of(contentTypeCheck, hstsCheck));

        assertThat(service.configuredChecks()).containsExactly(hstsCheck, contentTypeCheck);
    }

    @Test
    void reportsAConfiguredHeaderWithoutARegisteredCheck() {
        AssessmentService service = service(
                List.of("Strict-Transport-Security"),
                List.of(check("X-Content-Type-Options")));

        assertThatThrownBy(service::configuredChecks)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Strict-Transport-Security");
    }

    private static SecurityChecks check(String id) {
        SecurityChecks check = mock(SecurityChecks.class);
        when(check.checkId()).thenReturn(id);
        return check;
    }

    private static AssessmentService service(List<String> headers, List<SecurityChecks> checks) {
        return new AssessmentService(
                mock(HttpTransport.class),
                new AssessmentProperties(List.of(URI.create("http://localhost:8081"))),
                new SecurityCheckProperties(headers),
                checks);
    }
}
