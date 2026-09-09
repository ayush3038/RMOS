package com.rmos.controller;

import com.rmos.dto.MaintenanceTaskResponse;
import com.rmos.exception.ResourceNotFoundException;
import com.rmos.service.MaintenanceTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MaintenanceTaskController.class)
class MaintenanceTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaintenanceTaskService maintenanceTaskService;

    @Test
    void testGetAllTasks() throws Exception {
        MaintenanceTaskResponse task1 = new MaintenanceTaskResponse(1L, "TASK-1", "A-123", "MAINTENANCE", "HIGH", "IMMEDIATE", LocalDateTime.of(2026, 10, 10, 10, 0), 120, "PENDING", "SYSTEM");
        MaintenanceTaskResponse task2 = new MaintenanceTaskResponse(2L, "TASK-2", "A-124", "OPERATIONS", "LOW", "PLANNED", LocalDateTime.of(2026, 12, 10, 10, 0), 60, "COMPLETED", "USER");
        List<MaintenanceTaskResponse> tasks = Arrays.asList(task1, task2);

        when(maintenanceTaskService.getAllTasks()).thenReturn(tasks);

        mockMvc.perform(get("/api/maintenance-tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].taskCode").value("TASK-1"))
                .andExpect(jsonPath("$[0].assetId").value("A-123"))
                .andExpect(jsonPath("$[0].department").value("MAINTENANCE"))
                .andExpect(jsonPath("$[0].criticality").value("HIGH"))
                .andExpect(jsonPath("$[0].urgency").value("IMMEDIATE"))
                .andExpect(jsonPath("$[0].dueDate").exists())
                .andExpect(jsonPath("$[0].durationMinutes").value(120))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].sourceSystem").value("SYSTEM"));
    }

    @Test
    void testGetTaskById() throws Exception {
        MaintenanceTaskResponse task = new MaintenanceTaskResponse(1L, "TASK-1", "A-123", "MAINTENANCE", "HIGH", "IMMEDIATE", LocalDateTime.of(2026, 10, 10, 10, 0), 120, "PENDING", "SYSTEM");

        when(maintenanceTaskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/maintenance-tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.taskCode").value("TASK-1"))
                .andExpect(jsonPath("$.assetId").value("A-123"));
    }

    @Test
    void testGetTaskById_NotFound() throws Exception {
        when(maintenanceTaskService.getTaskById(anyLong())).thenThrow(new ResourceNotFoundException("Task not found"));

        mockMvc.perform(get("/api/maintenance-tasks/999"))
                .andExpect(status().isNotFound());
    }
}
