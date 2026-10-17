package com.sentinel.service;

import com.sentinel.checks.SecurityChecks;
import com.sentinel.config.AssessmentProperties;
import com.sentinel.exception.AssessmentTransportException;
import com.sentinel.http.HttpTransport;
import com.sentinel.model.AssessmentStatus;
import com.sentinel.model.CheckResult;
import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.OutcomeEnum;
import com.sentinel.model.ValidTarget;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AssessmentServiceExecutionTests {

    private static final URI ORIGIN = URI.create("https://localhost:8443");
    private static final URI TARGET = URI.create("https://localhost:8443/lab");

    @Test
    void fetchesOnceAndRunsChecksSequentiallyInRegistrationOrder() {
        HttpTransport transport = mock(HttpTransport.class);
        SecurityChecks first = mock(SecurityChecks.class);
        SecurityChecks second = mock(SecurityChecks.class);
        HttpResponseSnapshot observation = new HttpResponseSnapshot(TARGET, new HttpHeaders(), HttpStatus.OK);
        CheckResult firstResult = new CheckResult("first", OutcomeEnum.PASS, "present", "No change needed.");
        CheckResult secondResult = new CheckResult("second", OutcomeEnum.FINDING, "missing", "Set the header.");
        List<CheckResult> mutableResults = new ArrayList<>(List.of(firstResult));
        when(transport.createHttpResponseSnapshot(new ValidTarget(TARGET, true))).thenReturn(observation);
        when(first.check(observation)).thenReturn(mutableResults);
        when(second.check(observation)).thenReturn(List.of(secondResult));
        AssessmentService service = service(transport, List.of(first, second));

        var report = service.execute(TARGET);

        assertThat(report.target()).isEqualTo(TARGET);
        assertThat(report.status()).isEqualTo(AssessmentStatus.COMPLETED);
        assertThat(report.results()).containsExactly(firstResult, secondResult);
        InOrder order = inOrder(transport, first, second);
        order.verify(transport).createHttpResponseSnapshot(new ValidTarget(TARGET, true));
        order.verify(first).check(same(observation));
        order.verify(second).check(same(observation));
        order.verifyNoMoreInteractions();

        mutableResults.clear();
        assertThat(report.results()).containsExactly(firstResult, secondResult);
        assertThatThrownBy(() -> report.results().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void throwsTransportFailureWithoutRunningChecksWhenTransportReturnsNull() {
        HttpTransport transport = mock(HttpTransport.class);
        SecurityChecks check = mock(SecurityChecks.class);
        AssessmentService service = service(transport, List.of(check));

        assertThatThrownBy(() -> service.execute(TARGET))
                .isInstanceOf(AssessmentTransportException.class)
                .hasMessage("HTTP transport returned no response snapshot");
        InOrder order = inOrder(transport);
        order.verify(transport).createHttpResponseSnapshot(new ValidTarget(TARGET, true));
        order.verifyNoMoreInteractions();
        verifyNoInteractions(check);
    }

    private static AssessmentService service(HttpTransport transport, List<SecurityChecks> checks) {
        return new AssessmentService(transport, new AssessmentProperties(List.of(ORIGIN)), checks);
    }
}
