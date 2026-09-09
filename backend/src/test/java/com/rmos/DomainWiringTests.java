package com.rmos;

import com.rmos.domain.Asset;
import com.rmos.domain.MaintenanceTask;
import com.rmos.repository.AssetRepository;
import com.rmos.repository.MaintenanceTaskRepository;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainWiringTests {

    @Test
    void testEntitiesAreDiscovered() {
        // Prove that the classes are correctly annotated as JPA Entities
        // This ensures they will be discovered by @EntityScan automatically
        assertNotNull(Asset.class.getAnnotation(Entity.class), "Asset should be annotated with @Entity");
        assertNotNull(MaintenanceTask.class.getAnnotation(Entity.class),
                "MaintenanceTask should be annotated with @Entity");
    }

    @Test
    void testRepositoriesAreRecognizedBySpringData() {
        // Prove that the interfaces extend JpaRepository, which is exactly how Spring
        // Data recognizes them
        assertTrue(JpaRepository.class.isAssignableFrom(AssetRepository.class),
                "AssetRepository must extend JpaRepository");
        assertTrue(JpaRepository.class.isAssignableFrom(MaintenanceTaskRepository.class),
                "MaintenanceTaskRepository must extend JpaRepository");

        // Further prove they carry the correct stereotype
        assertNotNull(AssetRepository.class.getAnnotation(Repository.class),
                "AssetRepository should be annotated with @Repository");
        assertNotNull(MaintenanceTaskRepository.class.getAnnotation(Repository.class),
                "MaintenanceTaskRepository should be annotated with @Repository");
    }
}
