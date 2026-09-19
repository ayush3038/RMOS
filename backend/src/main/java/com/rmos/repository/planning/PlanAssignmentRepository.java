package com.rmos.repository.planning;

import com.rmos.domain.planning.PlanAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanAssignmentRepository extends JpaRepository<PlanAssignmentEntity, Long> {

    List<PlanAssignmentEntity> findByPlanningRunId(String planningRunId);
}
