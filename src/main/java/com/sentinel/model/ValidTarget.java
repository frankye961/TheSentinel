package com.sentinel.model;

import com.sentinel.exception.InvalidAssessmentTargetException;
import com.sentinel.exception.OutOfScopeTargetException;

import java.net.URI;

public record ValidTarget(@ValidScheme URI absoluteUri, boolean isInScope) {

    public ValidTarget {
        if (absoluteUri == null) {
            throw new InvalidAssessmentTargetException("absoluteUri has to be set");
        }
        String scheme = null;

        if(absoluteUri.getScheme() != null){
            scheme = absoluteUri.getScheme();
        } else {
            throw new InvalidAssessmentTargetException("absoluteUri.getScheme() has to be set");
        }

        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            throw new InvalidAssessmentTargetException("Scheme must be either 'http' or 'https'");
        }
        if (absoluteUri.getHost() == null) {
            throw new InvalidAssessmentTargetException("host has to be set");
        }
        if(!isInScope) {
            throw new OutOfScopeTargetException("URI not in scope");
        }
    }
}
