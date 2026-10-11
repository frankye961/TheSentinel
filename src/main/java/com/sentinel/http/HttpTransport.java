package com.sentinel.http;

import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.ValidTarget;

public interface HttpTransport {

    HttpResponseSnapshot createHttpResponseSnapshot(ValidTarget validTarget);
}
