package com.rmos.service.planning;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.fail;

class PlanningArchitectureTest {

    @Test
    void planningServiceShouldOnlyDependOnInterface() {
        Field[] fields = PlanningService.class.getDeclaredFields();
        for (Field field : fields) {
            Class<?> type = field.getType();
            if (type.getName().toLowerCase().contains("ortools")) {
                fail("PlanningService contains a dependency on OR-Tools: " + type.getName());
            }
            if (type.getName().contains("controller")) {
                fail("PlanningService contains a dependency on a controller: " + type.getName());
            }
        }
    }
}
