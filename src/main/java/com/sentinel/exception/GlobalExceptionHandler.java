package com.sentinel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OutOfScopeTargetException.class)
    public ProblemDetail handleOutOfScopeTarget(OutOfScopeTargetException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "The target is outside the permitted lab scope.");
        problem.setTitle("Target out of scope");
        return problem;
    }

    @ExceptionHandler(InvalidAssessmentTargetException.class)
    public ProblemDetail handleInvalidTarget(InvalidAssessmentTargetException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "The assessment target is invalid.");
        problem.setTitle("Invalid assessment target");
        return problem;
    }

    @ExceptionHandler(AssessmentTimeoutException.class)
    public ProblemDetail handleTimeout(AssessmentTimeoutException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.GATEWAY_TIMEOUT, "The target did not respond within the configured timeout.");
        problem.setTitle("Assessment request timed out");
        return problem;
    }

    @ExceptionHandler(AssessmentTransportException.class)
    public ProblemDetail handleTransportFailure(AssessmentTransportException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, "The target request could not be completed.");
        problem.setTitle("Assessment transport failure");
        return problem;
    }
}
