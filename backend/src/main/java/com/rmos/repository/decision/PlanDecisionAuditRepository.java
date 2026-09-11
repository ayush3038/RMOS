package com.rmos.repository.decision;

import com.rmos.domain.decision.PlanDecisionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanDecisionAuditRepository extends JpaRepository<PlanDecisionAudit, Long> {

    Optional<PlanDecisionAudit> findByDecisionId(String decisionId);

    List<PlanDecisionAudit> findByPlanningId(String planningId);
}
