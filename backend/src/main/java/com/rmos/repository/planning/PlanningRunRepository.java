package com.rmos.repository.planning;

import com.rmos.domain.planning.PlanningRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanningRunRepository extends JpaRepository<PlanningRunEntity, String> {

    // Find all versions of a specific plan structure by their ID or parent tree
    List<PlanningRunEntity> findByParentPlanId(String parentPlanId);
}
