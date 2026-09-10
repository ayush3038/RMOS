package com.rmos.controller.priority;

import com.rmos.dto.priority.PriorityAssessment;
import com.rmos.dto.priority.PriorityAssessmentRequest;
import com.rmos.service.priority.PriorityAssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/priority")
public class PriorityAssessmentController {

    private final PriorityAssessmentService assessmentService;

    public PriorityAssessmentController(PriorityAssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/assess")
    public ResponseEntity<PriorityAssessment> assessPriority(@Valid @RequestBody PriorityAssessmentRequest request) {
        PriorityAssessment assessment = assessmentService.assessPriority(request);
        return ResponseEntity.ok(assessment);
    }
}
