package com.sentinel.service;

import com.sentinel.checks.SecurityChecks;
import com.sentinel.config.AssessmentProperties;
import com.sentinel.http.HttpTransport;
import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.ValidTarget;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AssessmentService {

    private static final String HTTP =  "http";
    private static final String HTTPS = "https";
    private static final int HTTPS_PORT = 443;
    private static final int HTTP_PORT = 80;

    private final HttpTransport httpTransport;
    private final AssessmentProperties assessmentProperties;
    private List<SecurityChecks> securityChecks;

    public HttpResponseSnapshot fetch(URI uri) {
        boolean isInScope = isInScope(uri);
        ValidTarget validTarget = new ValidTarget(uri, isInScope);
        return httpTransport.createHttpResponseSnapshot(validTarget);
    }

    private boolean isInScope(URI uri) {
        if (uri == null || uri.getScheme() == null || uri.getHost() == null) {
            return false;
        }

        String scheme = uri.getScheme();
        if (!scheme.equalsIgnoreCase(HTTP) && !scheme.equalsIgnoreCase(HTTPS)) {
            return false;
        }

        return assessmentProperties
                .permittedOrigins()
                .stream()
                .anyMatch(origin -> origin.getScheme() != null
                        && origin.getHost() != null
                        && scheme.equalsIgnoreCase(origin.getScheme())
                        && uri.getHost().equalsIgnoreCase(origin.getHost())
                        && effectivePort(uri) == effectivePort(origin));
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return uri.getScheme().equalsIgnoreCase(HTTPS) ? HTTPS_PORT : HTTP_PORT;
    }
}
