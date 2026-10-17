package com.sentinel.service;

import com.sentinel.exception.AssessmentTimeoutException;
import com.sentinel.exception.AssessmentTransportException;
import com.sentinel.http.HttpTransport;
import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.ValidTarget;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

@Component
public class HttpTransportService implements HttpTransport {

    private final RestClient restClient;

    public HttpTransportService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public HttpResponseSnapshot createHttpResponseSnapshot(ValidTarget validTarget) {
        try {
            return restClient.get()
                    .uri(validTarget.absoluteUri())
                    .exchange((request, response) -> new HttpResponseSnapshot(
                            validTarget.absoluteUri(), response.getHeaders(), response.getStatusCode()));
        } catch (RestClientException exception) {
            if (isTimeout(exception)) {
                throw new AssessmentTimeoutException("HTTP assessment request timed out", exception);
            }
            throw new AssessmentTransportException("HTTP assessment request failed", exception);
        }
    }

    private static boolean isTimeout(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SocketTimeoutException || cause instanceof HttpTimeoutException) {
                return true;
            }
        }
        return false;
    }
}
