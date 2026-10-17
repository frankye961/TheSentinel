package com.sentinel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "assessment")
public record SecurityCheckProperties(List<String> headersToCheck) {
    public SecurityCheckProperties {
        if (headersToCheck.isEmpty()) throw new IllegalArgumentException("headersToCheck must not be empty");
        headersToCheck = List.copyOf(headersToCheck);
    }
}
