package com.rmos.service.decision;

import com.rmos.domain.decision.PlanDecisionAudit;
import com.rmos.dto.decision.PlanDecisionResult;
import com.rmos.repository.decision.PlanDecisionAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlanDecisionAuditService {

    private final PlanDecisionAuditRepository repository;

    public PlanDecisionAuditService(PlanDecisionAuditRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PlanDecisionResult auditDecision(PlanDecisionResult result) {
        String decisionId = UUID.randomUUID().toString();
        result.setDecisionId(decisionId);
        result.setDecidedAt(LocalDateTime.now());

        PlanDecisionAudit audit = new PlanDecisionAudit();
        audit.setDecisionId(decisionId);
        audit.setPlanningId(result.getPlanningId());
        audit.setPlanVersion(result.getPlanReference().getPlanVersion());
        audit.setReviewerId(result.getReviewerId());
        audit.setReviewerRole(result.getReviewerRole());
        audit.setDecision(result.getDecision());
        audit.setComment(result.getComment());
        audit.setDecidedAt(result.getDecidedAt());

        repository.save(audit);
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<PlanDecisionAudit> getDecision(String decisionId) {
        return repository.findByDecisionId(decisionId);
    }
}
