package com.sentinel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.List;

@ConfigurationProperties(prefix = "assessment")
public record AssessmentProperties(List<URI> permittedOrigins) {

    public AssessmentProperties {
        permittedOrigins = List.copyOf(permittedOrigins);
    }
}
