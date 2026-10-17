package com.sentinel.config;

import com.sentinel.model.ValidScheme;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;

@ConfigurationProperties(prefix = "assessment")
@Validated
public record AssessmentProperties(List<@ValidScheme URI> permittedOrigins) {

    public AssessmentProperties {
        if (permittedOrigins == null || permittedOrigins.isEmpty()) {
            throw new IllegalArgumentException("permittedOrigins must not be null or empty");
        }
        permittedOrigins = List.copyOf(permittedOrigins);
    }
}
