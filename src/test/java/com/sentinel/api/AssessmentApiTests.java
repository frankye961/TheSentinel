package com.sentinel.api;

import com.sentinel.model.AssessmentReport;
import com.sentinel.model.AssessmentStatus;
import com.sentinel.model.CheckResult;
import com.sentinel.model.OutcomeEnum;
import com.sentinel.service.AssessmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssessmentApi.class)
class AssessmentApiTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AssessmentService assessmentService;

    @Test
    void delegatesValidTargetToAssessmentService() throws Exception {
        URI target = URI.create("https://localhost:8443/lab");

        mockMvc.perform(post("/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assessmentURI\":\"https://localhost:8443/lab\"}"))
                .andExpect(status().isOk());

        verify(assessmentService).execute(target);
    }

    @Test
    void returnsStructuredReportForValidRequest() throws Exception {
        URI target = URI.create("https://localhost:8443/lab");
        CheckResult finding = new CheckResult("X-Content-Type-Options", OutcomeEnum.FINDING,
                "Header is missing.", "Set X-Content-Type-Options: nosniff.");
        when(assessmentService.execute(target))
                .thenReturn(new AssessmentReport(target, AssessmentStatus.COMPLETED, List.of(finding)));

        mockMvc.perform(post("/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assessmentURI\":\"https://localhost:8443/lab\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.target").value(target.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.results[0].checkId").value("X-Content-Type-Options"))
                .andExpect(jsonPath("$.results[0].outcome").value("FINDING"));

    }

    @Test
    void rejectsMissingTargetBeforeCallingService() throws Exception {
        mockMvc.perform(post("/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(assessmentService);
    }

    @Test
    void rejectsUnsupportedSchemeBeforeCallingService() throws Exception {
        mockMvc.perform(post("/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assessmentURI\":\"ftp://localhost:8443/lab\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(assessmentService);
    }
}
