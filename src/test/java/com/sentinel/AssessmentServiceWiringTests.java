package com.sentinel;

import com.sentinel.checks.SecurityChecks;
import com.sentinel.http.HttpTransport;
import com.sentinel.service.AssessmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(AssessmentServiceWiringTests.TestChecks.class)
class AssessmentServiceWiringTests {

    @MockitoBean(enforceOverride = true)
    private HttpTransport transport;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private SecurityChecks testCheck;

    @Test
    void serviceReceivesTheReplacedTransportAndRegisteredChecks() {
        AssessmentService service = context.getBean(AssessmentService.class);

        assertThat(context.getBean(HttpTransport.class)).isSameAs(transport);
        assertThat(ReflectionTestUtils.getField(service, "httpTransport")).isSameAs(transport);
        List<SecurityChecks> registeredChecks = List.copyOf(context.getBeansOfType(SecurityChecks.class).values());
        @SuppressWarnings("unchecked")
        List<SecurityChecks> injectedChecks =
                (List<SecurityChecks>) ReflectionTestUtils.getField(service, "securityChecks");
        assertThat(injectedChecks).containsExactlyInAnyOrderElementsOf(registeredChecks).contains(testCheck);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestChecks {

        @Bean
        SecurityChecks testCheck() {
            return mock(SecurityChecks.class);
        }
    }
}
