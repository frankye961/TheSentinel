package com.sentinel.checks;

import com.sentinel.model.CheckResult;
import com.sentinel.model.HttpResponseSnapshot;

import java.util.List;

public interface SecurityChecks {

    String checkId();

    List<CheckResult> check(HttpResponseSnapshot responseSnapshot);
}
