package com.sentinel.service;

import com.sentinel.config.AssessmentProperties;
import com.sentinel.config.SecurityCheckProperties;
import com.sentinel.http.HttpTransport;
import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.ValidTarget;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AssessmentServiceScopeTests {

    @Test
    void returnsTheObservationProducedByTheTransport() {
        HttpTransport transport = mock(HttpTransport.class);
        AssessmentService service = new AssessmentService(
                transport,
                new AssessmentProperties(List.of(URI.create("http://localhost:8081"))),
                new SecurityCheckProperties(List.of("X-Content-Type-Options")),
                List.of());
        URI uri = URI.create("http://localhost:8081/lab");
        HttpResponseSnapshot observation = new HttpResponseSnapshot(uri, new HttpHeaders(), HttpStatus.OK);
        when(transport.createHttpResponseSnapshot(new ValidTarget(uri, true))).thenReturn(observation);

        assertThat(service.fetch(uri)).isSameAs(observation);
        verify(transport).createHttpResponseSnapshot(new ValidTarget(uri, true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:8081/lab/page?mode=check",
            "http://LOCALHOST:8081/another-page",
            "HTTP://localhost:8081/uppercase-scheme"
    })
    void fetchesPathsWithinAPermittedOrigin(String target) {
        HttpTransport transport = mock(HttpTransport.class);
        AssessmentService service = new AssessmentService(
                transport,
                new AssessmentProperties(List.of(URI.create("http://localhost:8081"))),
                new SecurityCheckProperties(List.of("X-Content-Type-Options")),
                List.of());
        URI uri = URI.create(target);

        service.fetch(uri);

        verify(transport).createHttpResponseSnapshot(new ValidTarget(uri, true));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost.evil.test:8081/lab",
            "http://localhost:8082/lab",
            "https://localhost:8081/lab"
    })
    void rejectsTargetsOutsideThePermittedOriginBeforeFetching(String target) {
        HttpTransport transport = mock(HttpTransport.class);
        AssessmentService service = new AssessmentService(
                transport,
                new AssessmentProperties(List.of(URI.create("http://localhost:8081"))),
                new SecurityCheckProperties(List.of("X-Content-Type-Options")),
                List.of());

        assertThatThrownBy(() -> service.fetch(URI.create(target)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("URI not in scope");
        verifyNoInteractions(transport);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.test/lab",
            "https://example.test:443/lab",
            "HTTPS://example.test/lab"
    })
    void treatsImplicitAndExplicitDefaultPortsAsTheSameOrigin(String target) {
        HttpTransport transport = mock(HttpTransport.class);
        AssessmentService service = new AssessmentService(
                transport,
                new AssessmentProperties(List.of(URI.create("https://example.test"))),
                new SecurityCheckProperties(List.of("X-Content-Type-Options")),
                List.of());
        URI uri = URI.create(target);

        service.fetch(uri);

        verify(transport).createHttpResponseSnapshot(new ValidTarget(uri, true));
    }
}
