package com.sentinel.service;

import com.sentinel.config.AssessmentProperties;
import com.sentinel.config.SecurityCheckProperties;
import com.sentinel.http.HttpTransport;
import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.ValidTarget;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssessmentServiceConfiguredChecksTests {

    @Test
    void returnsConfiguredHeaderResultsInYamlOrder() {
        URI uri = URI.create("https://localhost:8443/lab");
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("Strict-Transport-Security", "max-age=31536000");
        SecurityChecksService checks = new SecurityChecksService(new SecurityCheckProperties(
                List.of("Strict-Transport-Security", "X-Content-Type-Options")));
        AssessmentService service = service(uri, headers, checks);

        assertThat(service.execute(uri).results())
                .extracting(result -> result.checkId())
                .containsExactly("Strict-Transport-Security", "X-Content-Type-Options");
    }

    @Test
    void reportsAConfiguredHeaderWithoutAnImplementation() {
        URI uri = URI.create("https://localhost:8443/lab");
        SecurityChecksService checks = new SecurityChecksService(new SecurityCheckProperties(
                List.of("Unsupported-Header")));
        AssessmentService service = service(uri, new HttpHeaders(), checks);

        assertThatThrownBy(() -> service.execute(uri))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsupported-Header");
    }

    private static AssessmentService service(URI uri, HttpHeaders headers, SecurityChecksService checks) {
        HttpTransport transport = mock(HttpTransport.class);
        when(transport.createHttpResponseSnapshot(new ValidTarget(uri, true)))
                .thenReturn(new HttpResponseSnapshot(uri, headers, HttpStatus.OK));
        return new AssessmentService(transport, new AssessmentProperties(List.of(URI.create("https://localhost:8443"))),
                List.of(checks));
    }
}
