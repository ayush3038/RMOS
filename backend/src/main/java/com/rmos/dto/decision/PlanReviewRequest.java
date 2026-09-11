package com.rmos.dto.decision;

import com.rmos.domain.decision.PlanDecision;
import com.rmos.domain.decision.ReviewerRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PlanReviewRequest {

    @NotBlank(message = "reviewerId is required")
    private String reviewerId;

    @NotNull(message = "reviewerRole is required")
    private ReviewerRole reviewerRole;

    @NotNull(message = "action is required")
    private PlanDecision action;

    @NotNull(message = "planReference is required")
    @Valid
    private PlanReference planReference;

    private String comment;

    @Valid
    private List<PlanModification> modifiedAssignments;

    // The requirements state planningId exists on the review request as well, but
    // PlanReference encapsulates it.
    // Adding planningId to bridge explicitly if needed although PlanReference holds
    // it.
    @NotBlank(message = "planningId is required on the request")
    private String planningId;

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

    public PlanDecision getAction() {
        return action;
    }

    public void setAction(PlanDecision action) {
        this.action = action;
    }

    public PlanReference getPlanReference() {
        return planReference;
    }

    public void setPlanReference(PlanReference planReference) {
        this.planReference = planReference;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<PlanModification> getModifiedAssignments() {
        return modifiedAssignments;
    }

    public void setModifiedAssignments(List<PlanModification> modifiedAssignments) {
        this.modifiedAssignments = modifiedAssignments;
    }

    public String getPlanningId() {
        return planningId;
    }

    public void setPlanningId(String planningId) {
        this.planningId = planningId;
    }
}
