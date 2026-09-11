package com.rmos.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanDecisionArchitectureTest {

    @Test
    void controllerNeverAccessesRepositoryDirectly() throws Exception {
        Path controllerPath = Paths.get("src/main/java/com/rmos/controller/decision/PlanDecisionController.java");
        assertTrue(Files.exists(controllerPath), "PlanDecisionController must exist");

        List<String> lines = Files.readAllLines(controllerPath);
        for (String line : lines) {
            assertFalse(line.contains("PlanDecisionAuditRepository"), "Controller must never touch Repo directly");
        }
    }

    @Test
    void serviceNeverAccessesRepositoryDirectly() throws Exception {
        Path servicePath = Paths.get("src/main/java/com/rmos/service/decision/PlanDecisionService.java");
        assertTrue(Files.exists(servicePath), "PlanDecisionService must exist");

        List<String> lines = Files.readAllLines(servicePath);
        for (String line : lines) {
            assertFalse(line.contains("PlanDecisionAuditRepository"),
                    "Service logic must remain abstract from JPA Repo hooks");
            assertFalse(line.contains("import org.springframework.data.jpa"), "Service must not rely on JPA imports");
        }
    }
}
