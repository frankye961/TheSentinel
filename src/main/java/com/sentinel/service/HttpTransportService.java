package com.sentinel.service;

import com.sentinel.http.HttpTransport;
import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.ValidTarget;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class HttpTransportService implements HttpTransport {

    private final RestClient restClient;

    public HttpTransportService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public HttpResponseSnapshot createHttpResponseSnapshot(ValidTarget validTarget) {
        return restClient.get()
                .uri(validTarget.absoluteUri())
                .exchange((request, response) -> new HttpResponseSnapshot(
                        validTarget.absoluteUri(), response.getHeaders(), response.getStatusCode()));
    }
}
