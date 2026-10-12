package com.sentinel.service;

import com.sentinel.checks.SecurityChecks;
import com.sentinel.config.SecurityCheckProperties;
import com.sentinel.model.CheckResult;
import com.sentinel.model.HttpResponseSnapshot;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentinel.model.OutcomeEnum.FINDING;
import static com.sentinel.model.OutcomeEnum.NOT_APPLICABLE;
import static com.sentinel.model.OutcomeEnum.PASS;

@Component
@AllArgsConstructor
public class SecurityChecksService implements SecurityChecks {

    private static final String HEADER = "X-Content-Type-Options";
    private static final String RECOMMENDATION = "Set X-Content-Type-Options: nosniff.";
    private static final String HSTS = "Strict-Transport-Security";
    private static final String HSTS_RECOMMENDATION = "Set Strict-Transport-Security with a positive max-age.";
    private static final Pattern MAX_AGE = Pattern.compile("(?i)^max-age\\s*=\\s*(?:\"([0-9]+)\"|([0-9]+))$");

    private final SecurityCheckProperties securityCheckProperties;

    @Override
    public String checkId() {
        return HEADER;
    }

    @Override
    public List<CheckResult> check(HttpResponseSnapshot responseSnapshot) {
        HttpHeaders headers = responseSnapshot.httpHeaders();
        List<CheckResult> checkResults = new ArrayList<>();
        for (String header : securityCheckProperties.headersToCheck()) {
            if (HEADER.equalsIgnoreCase(header)) {
                checkResults.add(checkContentTypeOptions(headers.get(HEADER)));
            } else if (HSTS.equalsIgnoreCase(header)) {
                checkResults.add(checkHsts(responseSnapshot, headers.get(HSTS)));
            } else {
                throw new IllegalStateException("No check implemented for configured header: " + header);
            }
        }
        return List.copyOf(checkResults);
    }

    private static CheckResult checkContentTypeOptions(List<String> values) {
        if (values == null || values.isEmpty()) {
            return new CheckResult(HEADER, FINDING, "Header is missing.", RECOMMENDATION);
        }

        String firstValue = values.getFirst().split(",", 2)[0].trim();
        if ("nosniff".equalsIgnoreCase(firstValue)) {
            return new CheckResult(HEADER, PASS, HEADER + ": " + firstValue, "No change needed.");
        }
        return new CheckResult(HEADER, FINDING, HEADER + ": " + values.getFirst(), RECOMMENDATION);
    }

    private static CheckResult checkHsts(HttpResponseSnapshot snapshot, List<String> values) {
        if (!"https".equalsIgnoreCase(snapshot.uri().getScheme())) {
            return new CheckResult(HSTS, NOT_APPLICABLE, "Target does not use HTTPS.", "No change needed.");
        }
        if (values == null || values.isEmpty()) {
            return new CheckResult(HSTS, FINDING, "Header is missing.", HSTS_RECOMMENDATION);
        }

        String value = values.getFirst();
        String maxAge = null;
        for (String directive : value.split(";", -1)) {
            String trimmed = directive.trim();
            if (trimmed.isEmpty()) {
                return new CheckResult(HSTS, FINDING, HSTS + ": " + value, HSTS_RECOMMENDATION);
            }
            if (trimmed.split("=", 2)[0].trim().equalsIgnoreCase("max-age")) {
                Matcher matcher = MAX_AGE.matcher(trimmed);
                if (maxAge != null || !matcher.matches()) {
                    return new CheckResult(HSTS, FINDING, HSTS + ": " + value, HSTS_RECOMMENDATION);
                }
                maxAge = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            }
        }

        if (maxAge == null || maxAge.chars().noneMatch(digit -> digit != '0')) {
            return new CheckResult(HSTS, FINDING, HSTS + ": " + value, HSTS_RECOMMENDATION);
        }
        return new CheckResult(HSTS, PASS, HSTS + ": " + value, "No change needed.");
    }
}
