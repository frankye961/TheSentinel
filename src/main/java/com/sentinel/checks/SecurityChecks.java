package com.sentinel.checks;

import com.sentinel.model.CheckResult;
import com.sentinel.model.HttpResponseSnapshot;

public interface SecurityChecks {

    CheckResult check(HttpResponseSnapshot responseSnapshot);
}
