package com.sentinel.config;

import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.ValidTarget;
import com.sentinel.service.HttpTransportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.InputStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(HttpTransportService.class)
class HttpTransportServiceTests {

    @Autowired
    private HttpTransportService transport;

    @Autowired
    private MockRestServiceServer server;

    @ParameterizedTest
    @ValueSource(ints = {200, 302, 404, 503})
    void returnsTargetStatusAndHeadersForEveryHttpResponse(int status) {
        URI uri = URI.create("http://localhost:8081/lab");
        server.expect(requestTo(uri))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatusCode.valueOf(status))
                        .header("X-Content-Type-Options", "nosniff")
                        .body("body is not part of the observation"));

        HttpResponseSnapshot snapshot = transport.createHttpResponseSnapshot(new ValidTarget(uri, true));

        assertThat(snapshot.uri()).isEqualTo(uri);
        assertThat(snapshot.httpStatus().value()).isEqualTo(status);
        assertThat(snapshot.httpHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
        server.verify();
    }

    @Test
    void doesNotReadTheResponseBody() {
        URI uri = URI.create("http://localhost:8081/lab");
        InputStream unreadableBody = new InputStream() {
            @Override
            public int read() {
                throw new AssertionError("The transport must not read the response body");
            }
        };
        server.expect(requestTo(uri))
                .andRespond(request -> new MockClientHttpResponse(unreadableBody, 200));

        HttpResponseSnapshot snapshot = transport.createHttpResponseSnapshot(new ValidTarget(uri, true));

        assertThat(snapshot.httpStatus().value()).isEqualTo(200);
        server.verify();
    }
}
