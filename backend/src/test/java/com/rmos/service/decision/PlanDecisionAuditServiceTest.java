package com.rmos.service.decision;

import com.rmos.domain.decision.PlanDecision;
import com.rmos.domain.decision.ReviewerRole;
import com.rmos.dto.decision.PlanReference;
import com.rmos.dto.decision.PlanDecisionResult;
import com.rmos.repository.decision.PlanDecisionAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class PlanDecisionAuditServiceTest {

    private PlanDecisionAuditRepository repo;
    private PlanDecisionAuditService service;

    @BeforeEach
    void setUp() {
        repo = mock(PlanDecisionAuditRepository.class);
        service = new PlanDecisionAuditService(repo);
    }

    @Test
    void testAuditDecisionInjectsUuidAndTimestamp() {
        PlanDecisionResult r = new PlanDecisionResult();
        r.setPlanningId("PLAN-01");
        r.setPlanReference(new PlanReference("PLAN-01", 1));
        r.setReviewerId("REV-A");
        r.setReviewerRole(ReviewerRole.OPERATOR);
        r.setDecision(PlanDecision.APPROVE);

        PlanDecisionResult out = service.auditDecision(r);

        assertNotNull(out.getDecisionId(), "Service must generate UUID");
        assertNotNull(out.getDecidedAt(), "Service must capture stamp");
        verify(repo, times(1)).save(ArgumentMatchers.any());
    }
}
