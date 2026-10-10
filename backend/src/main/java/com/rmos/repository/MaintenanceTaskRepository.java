package com.rmos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.rmos.domain.MaintenanceTask;
import com.rmos.domain.TaskStatus;

import java.util.List;

@Repository
public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, Long> {

    long countByStatus(TaskStatus status);

    List<MaintenanceTask> findTop5ByStatusOrderByDueDateAsc(TaskStatus status);
}
