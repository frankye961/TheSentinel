package com.sentinel.exception;

public class AssessmentTimeoutException extends AssessmentTransportException {

    public AssessmentTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
