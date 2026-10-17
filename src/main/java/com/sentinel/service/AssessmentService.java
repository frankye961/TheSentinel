package com.sentinel.service;

import com.sentinel.checks.SecurityChecks;
import com.sentinel.config.AssessmentProperties;
import com.sentinel.exception.AssessmentTransportException;
import com.sentinel.exception.InvalidAssessmentTargetException;
import com.sentinel.http.HttpTransport;
import com.sentinel.model.AssessmentReport;
import com.sentinel.model.AssessmentStatus;
import com.sentinel.model.CheckResult;
import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.ValidTarget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AssessmentService {

    private static final String HTTP =  "http";
    private static final String HTTPS = "https";
    private static final int HTTPS_PORT = 443;
    private static final int HTTP_PORT = 80;

    private final HttpTransport httpTransport;
    private final AssessmentProperties assessmentProperties;
    private final List<SecurityChecks> securityChecks;

    public AssessmentService(HttpTransport httpTransport, AssessmentProperties assessmentProperties,
                             List<SecurityChecks> securityChecks) {
        this.httpTransport = httpTransport;
        this.assessmentProperties = assessmentProperties;
        this.securityChecks = List.copyOf(securityChecks);
    }

    public AssessmentReport execute(URI uri) {
        if(uri.getUserInfo() != null) {
            throw new InvalidAssessmentTargetException("user info cannot be accepted");
        }
        HttpResponseSnapshot responseSnapshot = fetch(uri);
        if (responseSnapshot == null) {
            log.error("Assessment response is null");
            throw new AssessmentTransportException("HTTP transport returned no response snapshot");
        }
        List<CheckResult> results = new ArrayList<>();
        for (SecurityChecks check : securityChecks) {
            results.addAll(check.check(responseSnapshot));
        }
        return new AssessmentReport(uri, AssessmentStatus.COMPLETED, results);
    }

    private HttpResponseSnapshot fetch(URI uri) {
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
