package com.sentinel.model;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class SchemeValidator implements ConstraintValidator<ValidScheme, URI> {

    private Set<String> permittedSchemes;

    @Override
    public void initialize(ValidScheme constraintAnnotation) {
        permittedSchemes = Arrays.stream(constraintAnnotation.schemes())
                .map(scheme -> scheme.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isValid(URI value, ConstraintValidatorContext context) {
        return value == null || (value.getScheme() != null
                && permittedSchemes.contains(value.getScheme().toLowerCase(Locale.ROOT)));
    }
}
