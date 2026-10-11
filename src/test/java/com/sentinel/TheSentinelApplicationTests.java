package com.sentinel;

import com.sentinel.service.HttpTransportService;
import com.sentinel.config.AssessmentProperties;
import com.sentinel.config.SecurityCheckProperties;
import com.sentinel.http.HttpTransport;
import com.sentinel.service.AssessmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TheSentinelApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AssessmentProperties assessmentProperties;

    @Autowired
    private SecurityCheckProperties securityCheckProperties;

    @Test
    void contextStartsAndWiresTheTransportIntoTheService() {
        HttpTransport transport = context.getBean(HttpTransport.class);
        AssessmentService service = context.getBean(AssessmentService.class);

        assertThat(transport).isInstanceOf(HttpTransportService.class);
        assertThat(ReflectionTestUtils.getField(service, "httpTransport")).isSameAs(transport);
    }

    @Test
    void bindsPermittedOriginsFromApplicationYaml() {
        assertThat(assessmentProperties.permittedOrigins()).containsExactly(
                URI.create("http://localhost:8081"),
                URI.create("https://localhost:8443"));
    }

    @Test
    void bindsConfiguredHeaderNamesFromApplicationYaml() {
        assertThat(securityCheckProperties.headersToCheck()).containsExactly(
                "X-Content-Type-Options", "Strict-Transport-Security");
    }
}
