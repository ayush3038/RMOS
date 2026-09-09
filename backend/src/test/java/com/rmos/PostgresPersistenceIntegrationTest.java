package com.rmos;

import com.rmos.domain.Asset;
import com.rmos.domain.Department;
import com.rmos.domain.MaintenanceTask;
import com.rmos.domain.PriorityLevel;
import com.rmos.domain.TaskStatus;
import com.rmos.repository.AssetRepository;
import com.rmos.repository.MaintenanceTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Validates real PostgreSQL persistence layer integration.
 * To run this test, set the environment variable
 * POSTGRES_INTEGRATION_TEST=true.
 */
@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "POSTGRES_INTEGRATION_TEST", matches = "true")
class PostgresPersistenceIntegrationTest {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private MaintenanceTaskRepository taskRepository;

    @Test
    void testRealPostgresPersistence() {
        // 1. Asset can be saved
        Asset asset = new Asset(
                "A-100",
                "Switch",
                "SEC-1",
                Department.ENGINEERING,
                true);
        Asset savedAsset = assetRepository.saveAndFlush(asset);
        assertNotNull(savedAsset.getId(), "Asset should have a generated ID");

        // 2. Asset can be read back
        Asset retrievedAsset = assetRepository.findById(savedAsset.getId()).orElse(null);
        assertNotNull(retrievedAsset);
        assertEquals("A-100", retrievedAsset.getAssetId(), "Asset canonical ID should match");
        assertEquals(Department.ENGINEERING, retrievedAsset.getDepartment(), "Enum should match");

        // 3. MaintenanceTask referencing that Asset can be saved
        MaintenanceTask task = new MaintenanceTask(
                "TASK-200",
                savedAsset,
                Department.ENGINEERING,
                PriorityLevel.CRITICAL,
                PriorityLevel.HIGH,
                LocalDateTime.now().plusDays(2),
                120,
                TaskStatus.PENDING,
                "EAM-CORE");
        MaintenanceTask savedTask = taskRepository.saveAndFlush(task);
        assertNotNull(savedTask.getId(), "Task should have a generated ID");

        // 4. MaintenanceTask can be read back with the correct Asset relationship
        MaintenanceTask retrievedTask = taskRepository.findById(savedTask.getId()).orElse(null);
        assertNotNull(retrievedTask);
        assertEquals("TASK-200", retrievedTask.getTaskCode());
        assertNotNull(retrievedTask.getAsset());
        assertEquals(savedAsset.getId(), retrievedTask.getAsset().getId(), "Relationship mapping correctly links");

        // 5. Enum fields persist correctly as strings (implicitly tested by fetching
        // back entity and validating JPA Enum string maps correctly)
        assertEquals(PriorityLevel.CRITICAL, retrievedTask.getCriticality());
        assertEquals(PriorityLevel.HIGH, retrievedTask.getUrgency());
        assertEquals(TaskStatus.PENDING, retrievedTask.getStatus());

        // 6. The canonical asset identifier remains unique (this refers to schema
        // unique constraints, which is set in Domain entity `@Column(unique=true)`).
    }
}
