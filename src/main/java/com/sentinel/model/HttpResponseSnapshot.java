package com.sentinel.model;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

import java.net.URI;

public record HttpResponseSnapshot(URI uri, HttpHeaders httpHeaders, HttpStatusCode httpStatus) {

    public HttpResponseSnapshot(URI uri, HttpHeaders httpHeaders,  HttpStatusCode httpStatus) {
        if (httpHeaders == null) {
            throw new IllegalArgumentException("httpHeaders cannot be null");
        }
        this.uri = uri;
        this.httpHeaders = HttpHeaders.readOnlyHttpHeaders(HttpHeaders.copyOf(httpHeaders));
        this.httpStatus = httpStatus;
    }
}
