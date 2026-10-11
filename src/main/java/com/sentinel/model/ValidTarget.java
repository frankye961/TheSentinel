package com.sentinel.model;

import java.net.URI;

public record ValidTarget(URI absoluteUri, boolean isInScope) {

    public ValidTarget {
        if (absoluteUri == null) {
            throw new NullPointerException("absoluteUri has to be set");
        }
        String scheme = absoluteUri.getScheme();

        if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
            throw new IllegalArgumentException("Scheme must be either 'http' or 'https'");
        }
        if (absoluteUri.getHost() == null) {
            throw new IllegalArgumentException("host has to be set");
        }
        if(!isInScope) {
            throw new IllegalArgumentException("URI not in scope");
        }
    }
}
