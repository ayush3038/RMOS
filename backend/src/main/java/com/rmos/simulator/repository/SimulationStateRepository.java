package com.rmos.simulator.repository;

import com.rmos.simulator.domain.SimulationStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulationStateRepository extends JpaRepository<SimulationStateEntity, String> {
}
