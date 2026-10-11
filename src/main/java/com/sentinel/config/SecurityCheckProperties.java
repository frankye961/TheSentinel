package com.sentinel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "assessment")
public record SecurityCheckProperties(List<String> headersToCheck) {
    public SecurityCheckProperties {
        headersToCheck = List.copyOf(headersToCheck);
    }
}
