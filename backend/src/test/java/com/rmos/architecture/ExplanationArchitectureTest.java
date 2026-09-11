package com.rmos.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExplanationArchitectureTest {

    @Test
    void explanationModelDoesNotDependOnIllegalPackages() throws Exception {
        Path modelPath = Paths.get("src/main/java/com/rmos/service/ai/ExplanationModel.java");
        assertTrue(Files.exists(modelPath), "ExplanationModel must exist");

        List<String> lines = Files.readAllLines(modelPath);
        for (String line : lines) {
            assertFalse(line.contains("import org.springframework.web"),
                    "ExplanationModel should not import Spring Web");
            assertFalse(line.contains("import org.springframework.data.jpa"), "ExplanationModel should not import JPA");
            assertFalse(line.contains("import jakarta.persistence"), "ExplanationModel should not import JPA entities");
            assertFalse(line.contains("import com.google.ortools"), "ExplanationModel should not import ORTools");
        }
    }

    @Test
    void serviceDependsOnAbstraction() throws Exception {
        Path servicePath = Paths.get("src/main/java/com/rmos/service/ai/ExplanationService.java");
        List<String> lines = Files.readAllLines(servicePath);
        assertTrue(lines.stream().anyMatch(l -> l.contains("ExplanationModel")),
                "ExpService must rely on Model abstract");
    }

    @Test
    void nimAdapterArchitectureCheck() throws Exception {
        Path nimPath = Paths.get("src/main/java/com/rmos/service/ai/NvidiaNimExplanationModel.java");
        List<String> lines = Files.readAllLines(nimPath);
        for (String line : lines) {
            assertFalse(line.contains("import org.springframework.data.jpa"), "NIM should not know DB");
        }
    }
}
