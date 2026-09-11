package com.rmos.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetRiskArchitectureTest {

    @Test
    void assetRiskModelShouldNotDependOnWebOrPersistence() throws Exception {
        Path interfacePath = Paths.get("src/main/java/com/rmos/service/risk/AssetRiskModel.java");
        assertTrue(Files.exists(interfacePath), "AssetRiskModel must exist");

        List<String> lines = Files.readAllLines(interfacePath);
        for (String line : lines) {
            assertFalse(line.contains("import org.springframework.web"), "AssetRiskModel should not import Spring Web");
            assertFalse(line.contains("import org.springframework.data.jpa"), "AssetRiskModel should not import JPA");
            assertFalse(line.contains("import jakarta.persistence"), "AssetRiskModel should not import JPA entities");
            assertFalse(line.contains("import com.google.ortools"), "AssetRiskModel should not import ORTools");
        }
    }

    @Test
    void assetRiskAssessmentServiceDependsOnModel() throws Exception {
        Path servicePath = Paths.get("src/main/java/com/rmos/service/risk/AssetRiskAssessmentService.java");
        assertTrue(Files.exists(servicePath), "AssetRiskAssessmentService must exist");

        List<String> lines = Files.readAllLines(servicePath);
        boolean importsModel = lines.stream().anyMatch(l -> l.contains("AssetRiskModel"));
        assertTrue(importsModel, "Service must depend on AssetRiskModel abstraction");
    }
}
