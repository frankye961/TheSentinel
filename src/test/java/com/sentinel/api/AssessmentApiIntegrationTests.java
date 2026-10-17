package com.sentinel.api;

import com.sentinel.http.HttpTransport;
import com.sentinel.model.HttpResponseSnapshot;
import com.sentinel.model.ValidTarget;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssessmentApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(enforceOverride = true)
    private HttpTransport transport;

    @Test
    void assessesAnInScopeTargetThroughServiceAndChecks() throws Exception {
        URI target = URI.create("http://localhost:8081/lab");
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content-Type-Options", "nosniff");
        when(transport.createHttpResponseSnapshot(new ValidTarget(target, true)))
                .thenReturn(new HttpResponseSnapshot(target, headers, HttpStatus.OK));

        mockMvc.perform(post("/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assessmentURI\":\"http://localhost:8081/lab\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.target").value(target.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.results[0].checkId").value("X-Content-Type-Options"))
                .andExpect(jsonPath("$.results[0].outcome").value("PASS"))
                .andExpect(jsonPath("$.results[1].checkId").value("Strict-Transport-Security"))
                .andExpect(jsonPath("$.results[1].outcome").value("NOT_APPLICABLE"));

        verify(transport).createHttpResponseSnapshot(new ValidTarget(target, true));
    }

    @Test
    void mapsMissingTransportSnapshotToBadGateway() throws Exception {
        URI target = URI.create("http://localhost:8081/lab");

        mockMvc.perform(post("/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assessmentURI\":\"http://localhost:8081/lab\"}"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.title").value("Assessment transport failure"))
                .andExpect(jsonPath("$.detail").value("The target request could not be completed."));

        verify(transport).createHttpResponseSnapshot(new ValidTarget(target, true));
    }
}
