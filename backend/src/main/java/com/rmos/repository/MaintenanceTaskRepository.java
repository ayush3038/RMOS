package com.rmos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rmos.domain.MaintenanceTask;

@Repository
public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, Long> {
}
