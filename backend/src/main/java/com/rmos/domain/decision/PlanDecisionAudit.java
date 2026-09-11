package com.rmos.domain.decision;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "plan_decision_audits")
public class PlanDecisionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String decisionId;

    @Column(nullable = false)
    private String planningId;

    @Column(nullable = false)
    private Integer planVersion;

    @Column(nullable = false)
    private String reviewerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewerRole reviewerRole;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PlanDecision decision;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private LocalDateTime decidedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getPlanVersion() {
        return planVersion;
    }

    public void setPlanVersion(Integer planVersion) {
        this.planVersion = planVersion;
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

    public PlanDecision getDecision() {
        return decision;
    }

    public void setDecision(PlanDecision decision) {
        this.decision = decision;
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
