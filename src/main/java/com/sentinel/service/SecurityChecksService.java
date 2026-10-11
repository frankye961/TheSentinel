package com.sentinel.service;

import com.sentinel.checks.SecurityChecks;
import com.sentinel.model.CheckResult;
import com.sentinel.model.HttpResponseSnapshot;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.sentinel.model.OutcomeEnum.FINDING;
import static com.sentinel.model.OutcomeEnum.PASS;

@Component
public class SecurityChecksService implements SecurityChecks {

    private static final String HEADER = "X-Content-Type-Options";
    private static final String RECOMMENDATION = "Set X-Content-Type-Options: nosniff.";

    @Override
    public String checkId() {
        return HEADER;
    }

    @Override
    public CheckResult check(HttpResponseSnapshot responseSnapshot) {
        HttpHeaders headers = responseSnapshot.httpHeaders();
        List<String> values = headers.get(HEADER);
        if (values == null || values.isEmpty()) {
            return new CheckResult(HEADER, FINDING, "Header is missing.", RECOMMENDATION);
        }

        String firstValue = values.getFirst().split(",", 2)[0].trim();
        if ("nosniff".equalsIgnoreCase(firstValue)) {
            return new CheckResult(HEADER, PASS, HEADER + ": " + firstValue, "No change needed.");
        }

        return new CheckResult(HEADER, FINDING, HEADER + ": " + values.getFirst(), RECOMMENDATION);
    }

}
