package com.rmos.service;

import com.rmos.dto.MaintenanceTaskResponse;
import com.rmos.exception.ResourceNotFoundException;
import com.rmos.repository.MaintenanceTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceTaskService {

    private final MaintenanceTaskRepository maintenanceTaskRepository;

    public MaintenanceTaskService(MaintenanceTaskRepository maintenanceTaskRepository) {
        this.maintenanceTaskRepository = maintenanceTaskRepository;
    }

    public List<MaintenanceTaskResponse> getAllTasks() {
        return maintenanceTaskRepository.findAll().stream()
                .map(MaintenanceTaskResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public MaintenanceTaskResponse getTaskById(Long id) {
        return maintenanceTaskRepository.findById(id)
                .map(MaintenanceTaskResponse::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance task not found with id: " + id));
    }
}
