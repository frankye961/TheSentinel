package com.sentinel.model;

import java.util.Objects;

public record CheckResult(
        String checkId,
        OutcomeEnum outcome,
        String evidence,
        String recommendation
) {
    public CheckResult {
        Objects.requireNonNull(checkId, "checkId");
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(evidence, "evidence");
        Objects.requireNonNull(recommendation, "recommendation");
    }
}
