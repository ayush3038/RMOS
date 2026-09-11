package com.rmos.service.decision;

import com.rmos.domain.decision.DecisionStatus;
import com.rmos.domain.decision.PlanDecision;
import com.rmos.dto.decision.PlanDecisionResult;
import com.rmos.dto.decision.PlanReviewRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PlanDecisionService {

    private final PlanDecisionAuditService auditService;

    public PlanDecisionService(PlanDecisionAuditService auditService) {
        this.auditService = auditService;
    }

    public PlanDecisionResult processDecision(PlanReviewRequest request) {

        if (request.getAction() == PlanDecision.REJECT && !StringUtils.hasText(request.getComment())) {
            throw new IllegalArgumentException("A comment is explicitly required when rejecting a planning proposal.");
        }

        if (request.getAction() == PlanDecision.MODIFY) {
            if (!StringUtils.hasText(request.getComment())) {
                throw new IllegalArgumentException("A comment is explicitly required when requesting modifications.");
            }
            if (request.getModifiedAssignments() == null || request.getModifiedAssignments().isEmpty()) {
                throw new IllegalArgumentException(
                        "At least one PlanModification is required when the action is MODIFY.");
            }
        }

        PlanDecisionResult result = new PlanDecisionResult();
        result.setPlanningId(request.getPlanningId());
        result.setPlanReference(request.getPlanReference());
        result.setDecision(request.getAction());
        result.setReviewerId(request.getReviewerId());
        result.setReviewerRole(request.getReviewerRole());
        result.setComment(request.getComment());

        switch (request.getAction()) {
            case APPROVE -> result.setStatus(DecisionStatus.ACCEPTED);
            case REJECT -> result.setStatus(DecisionStatus.REJECTED);
            case MODIFY -> result.setStatus(DecisionStatus.REQUIRES_MODIFICATION);
        }

        return auditService.auditDecision(result);
    }
}
