package com.rmos.dto.decision;

import com.rmos.domain.decision.DecisionStatus;
import com.rmos.domain.decision.PlanDecision;
import com.rmos.domain.decision.ReviewerRole;
import java.time.LocalDateTime;

public class PlanDecisionResult {

    private String decisionId;
    private String planningId;
    private PlanReference planReference;
    private PlanDecision decision;
    private DecisionStatus status;
    private String reviewerId;
    private ReviewerRole reviewerRole;
    private String comment;
    private LocalDateTime decidedAt;

    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    public String getPlanningId() {
        return planningId;
    }

    public void setPlanningId(String planningId) {
        this.planningId = planningId;
    }

    public PlanReference getPlanReference() {
        return planReference;
    }

    public void setPlanReference(PlanReference planReference) {
        this.planReference = planReference;
    }

    public PlanDecision getDecision() {
        return decision;
    }

    public void setDecision(PlanDecision decision) {
        this.decision = decision;
    }

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(DecisionStatus status) {
        this.status = status;
    }

    public String getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    public ReviewerRole getReviewerRole() {
        return reviewerRole;
    }

    public void setReviewerRole(ReviewerRole reviewerRole) {
        this.reviewerRole = reviewerRole;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}
