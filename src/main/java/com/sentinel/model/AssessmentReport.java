package com.sentinel.model;

import java.net.URI;
import java.util.List;
import java.util.Objects;

public record AssessmentReport(URI target, AssessmentStatus status, List<CheckResult> results) {

    public AssessmentReport {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(status, "status");
        results = List.copyOf(results);
    }
}
