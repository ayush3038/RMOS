package com.rmos.service.decision;

import com.rmos.domain.decision.DecisionStatus;
import com.rmos.domain.decision.PlanDecision;
import com.rmos.domain.decision.ReviewerRole;
import com.rmos.dto.decision.PlanModification;
import com.rmos.dto.decision.PlanReference;
import com.rmos.dto.decision.PlanReviewRequest;
import com.rmos.dto.decision.PlanDecisionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlanDecisionServiceTest {

    private PlanDecisionAuditService auditService;
    private PlanDecisionService service;

    @BeforeEach
    void setUp() {
        auditService = mock(PlanDecisionAuditService.class);
        service = new PlanDecisionService(auditService);

        when(auditService.auditDecision(any())).thenAnswer(i -> i.getArguments()[0]); // Echo Result
    }

    private PlanReviewRequest baseReq() {
        PlanReviewRequest r = new PlanReviewRequest();
        r.setReviewerId("REV-1");
        r.setReviewerRole(ReviewerRole.ADMIN);
        r.setPlanningId("P1");
        r.setPlanReference(new PlanReference("P1", 1));
        return r;
    }

    @Test
    void approveValidNoComment() {
        PlanReviewRequest r = baseReq();
        r.setAction(PlanDecision.APPROVE);

        PlanDecisionResult result = service.processDecision(r);

        assertEquals(DecisionStatus.ACCEPTED, result.getStatus());
        assertEquals(PlanDecision.APPROVE, result.getDecision());
        verify(auditService, times(1)).auditDecision(any());
    }

    @Test
    void rejectRequiresComment() {
        PlanReviewRequest r = baseReq();
        r.setAction(PlanDecision.REJECT);
        r.setComment(""); // Blank meaning invalid

        assertThrows(IllegalArgumentException.class, () -> service.processDecision(r));
    }

    @Test
    void modifyRequiresCommentAndModificationList() {
        PlanReviewRequest r = baseReq();
        r.setAction(PlanDecision.MODIFY);
        r.setComment("Need shift");

        // Fails due to no modifiedAssignments
        assertThrows(IllegalArgumentException.class, () -> service.processDecision(r));

        // Setup modification
        PlanModification pm = new PlanModification();
        pm.setTaskId("T1");
        pm.setReason("Overlap");
        r.setModifiedAssignments(Collections.singletonList(pm));

        PlanDecisionResult result = service.processDecision(r);
        assertEquals(DecisionStatus.REQUIRES_MODIFICATION, result.getStatus());
    }
}
