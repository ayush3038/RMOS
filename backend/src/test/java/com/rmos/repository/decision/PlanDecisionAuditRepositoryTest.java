package com.rmos.repository.decision;

import com.rmos.domain.decision.PlanDecision;
import com.rmos.domain.decision.PlanDecisionAudit;
import com.rmos.domain.decision.ReviewerRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "POSTGRES_INTEGRATION_TEST", matches = "true")
class PlanDecisionAuditRepositoryTest {

    @Autowired
    private PlanDecisionAuditRepository repository;

    @Test
    void testSaveAndRetrieveAudit() {
        String decisionId = UUID.randomUUID().toString();

        PlanDecisionAudit audit = new PlanDecisionAudit();
        audit.setDecisionId(decisionId);
        audit.setPlanningId("PLAN-99");
        audit.setPlanVersion(1);
        audit.setReviewerId("USR-777");
        audit.setReviewerRole(ReviewerRole.SUPERVISOR);
        audit.setDecision(PlanDecision.APPROVE);
        audit.setDecidedAt(LocalDateTime.now());

        PlanDecisionAudit saved = repository.save(audit);
        assertNotNull(saved.getId(), "Generated Persistence ID must exist");

        Optional<PlanDecisionAudit> retrieved = repository.findByDecisionId(decisionId);
        assertTrue(retrieved.isPresent());
        assertEquals(ReviewerRole.SUPERVISOR, retrieved.get().getReviewerRole());
    }
}
