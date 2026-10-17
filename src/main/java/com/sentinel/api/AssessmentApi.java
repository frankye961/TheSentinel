package com.sentinel.api;

import com.sentinel.api.model.AssessmentDTO;
import com.sentinel.model.AssessmentReport;
import com.sentinel.service.AssessmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AssessmentApi {

    private final AssessmentService assessmentService;

    @PostMapping("/assessments")
    public ResponseEntity<AssessmentReport> createAssessment(@RequestBody @Valid AssessmentDTO assessmentDTO) {
        var report = assessmentService.execute(assessmentDTO.getAssessmentURI());
        return ResponseEntity.ok(report);
    }
}
