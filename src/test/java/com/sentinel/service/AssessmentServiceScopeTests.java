package com.sentinel.service;

import com.sentinel.checks.SecurityChecks;
import com.sentinel.config.AssessmentProperties;
import com.sentinel.exception.InvalidAssessmentTargetException;
import com.sentinel.http.HttpTransport;
import com.sentinel.model.AssessmentStatus;
import com.sentinel.model.CheckResult;
import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.OutcomeEnum;
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
    void rejectsNullTargetBeforeFetching() {
        HttpTransport transport = mock(HttpTransport.class);
        AssessmentService service = new AssessmentService(
                transport,
                new AssessmentProperties(List.of(URI.create("http://localhost:8081"))),
                List.of());

        assertThatThrownBy(() -> service.execute(null))
                .isInstanceOf(InvalidAssessmentTargetException.class)
                .hasMessage("uri missing");
        verifyNoInteractions(transport);
    }

    @Test
    void rejectsTargetWithUserInfoBeforeFetching() {
        HttpTransport transport = mock(HttpTransport.class);
        AssessmentService service = new AssessmentService(
                transport,
                new AssessmentProperties(List.of(URI.create("http://localhost:8081"))),
                List.of());

        assertThatThrownBy(() -> service.execute(URI.create("http://user:secret@localhost:8081/lab")))
                .isInstanceOf(InvalidAssessmentTargetException.class)
                .hasMessage("user info cannot be accepted");
        verifyNoInteractions(transport);
    }

    @Test
    void returnsAReportForTheObservationProducedByTheTransport() {
        HttpTransport transport = mock(HttpTransport.class);
        SecurityChecks check = mock(SecurityChecks.class);
        AssessmentService service = new AssessmentService(
                transport,
                new AssessmentProperties(List.of(URI.create("http://localhost:8081"))),
                List.of(check));
        URI uri = URI.create("http://localhost:8081/lab");
        HttpResponseSnapshot observation = new HttpResponseSnapshot(uri, new HttpHeaders(), HttpStatus.OK);
        CheckResult result = new CheckResult("X-Content-Type-Options", OutcomeEnum.PASS, "nosniff", "No change needed.");
        when(transport.createHttpResponseSnapshot(new ValidTarget(uri, true))).thenReturn(observation);
        when(check.check(observation)).thenReturn(List.of(result));

        var report = service.execute(uri);

        assertThat(report.target()).isEqualTo(uri);
        assertThat(report.status()).isEqualTo(AssessmentStatus.COMPLETED);
        assertThat(report.results()).containsExactly(result);
        verify(transport).createHttpResponseSnapshot(new ValidTarget(uri, true));
        verify(check).check(observation);
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
                List.of());
        URI uri = URI.create(target);
        when(transport.createHttpResponseSnapshot(new ValidTarget(uri, true)))
                .thenReturn(new HttpResponseSnapshot(uri, new HttpHeaders(), HttpStatus.OK));

        assertThat(service.execute(uri).status()).isEqualTo(AssessmentStatus.COMPLETED);

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
                List.of());

        assertThatThrownBy(() -> service.execute(URI.create(target)))
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
                List.of());
        URI uri = URI.create(target);
        when(transport.createHttpResponseSnapshot(new ValidTarget(uri, true)))
                .thenReturn(new HttpResponseSnapshot(uri, new HttpHeaders(), HttpStatus.OK));

        assertThat(service.execute(uri).status()).isEqualTo(AssessmentStatus.COMPLETED);

        verify(transport).createHttpResponseSnapshot(new ValidTarget(uri, true));
    }
}
