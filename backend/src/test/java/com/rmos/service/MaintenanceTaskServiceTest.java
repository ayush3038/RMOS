package com.rmos.service;

import com.rmos.domain.Asset;
import com.rmos.domain.Department;
import com.rmos.domain.MaintenanceTask;
import com.rmos.domain.PriorityLevel;
import com.rmos.domain.TaskStatus;
import com.rmos.dto.MaintenanceTaskResponse;
import com.rmos.exception.ResourceNotFoundException;
import com.rmos.repository.MaintenanceTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceTaskServiceTest {

    @Mock
    private MaintenanceTaskRepository taskRepository;

    @InjectMocks
    private MaintenanceTaskService taskService;

    @Test
    void testGetAllTasks() {
        Asset asset = new Asset("ASSET-001", "Signal", "SEC-01", Department.S_AND_T, true);
        MaintenanceTask task = new MaintenanceTask(
                "TASK-001", asset, Department.S_AND_T,
                PriorityLevel.HIGH, PriorityLevel.CRITICAL, LocalDateTime.now(), 30,
                TaskStatus.PENDING, "SMMS");
        task.setId(1L);

        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<MaintenanceTaskResponse> responses = taskService.getAllTasks();

        assertEquals(1, responses.size());
        assertEquals("TASK-001", responses.get(0).getTaskCode());
        assertEquals("ASSET-001", responses.get(0).getAssetId());
        assertEquals("HIGH", responses.get(0).getCriticality());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void testGetTaskByIdNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(99L));
        verify(taskRepository, times(1)).findById(99L);
    }
}
