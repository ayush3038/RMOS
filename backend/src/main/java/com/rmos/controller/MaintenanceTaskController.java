package com.rmos.controller;

import com.rmos.dto.MaintenanceTaskResponse;
import com.rmos.service.MaintenanceTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-tasks")
public class MaintenanceTaskController {

    private final MaintenanceTaskService maintenanceTaskService;

    public MaintenanceTaskController(MaintenanceTaskService maintenanceTaskService) {
        this.maintenanceTaskService = maintenanceTaskService;
    }

    @GetMapping
    public List<MaintenanceTaskResponse> getAllTasks() {
        return maintenanceTaskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public MaintenanceTaskResponse getTaskById(@PathVariable Long id) {
        return maintenanceTaskService.getTaskById(id);
    }
}
