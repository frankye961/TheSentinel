package com.sentinel.service;

import com.sentinel.checks.SecurityChecks;
import com.sentinel.model.CheckResult;
import com.sentinel.model.HttpResponseSnapshot;
import org.springframework.stereotype.Component;

@Component
public class SecurityChecksService implements SecurityChecks {

    @Override
    public CheckResult check(HttpResponseSnapshot responseSnapshot) {
        return null;
    }
}
