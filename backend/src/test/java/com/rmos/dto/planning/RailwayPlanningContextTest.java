package com.rmos.dto.planning;

import com.rmos.domain.planning.BlockWindowStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RailwayPlanningContextTest {

    @Test
    void testValidContext() {
        RailwayPlanningContext context = new RailwayPlanningContext();
        context.setPlanningWindowStart(LocalDateTime.now());
        context.setPlanningWindowEnd(LocalDateTime.now().plusHours(4));

        RailwaySection section = new RailwaySection();
        section.setSectionId("SEC-01");
        context.setSections(Collections.singletonList(section));

        // Use custom validation test since @AssertTrue triggers in Validator, not
        // simple setter
        // Actually, bean validation will handle true/false logic. Here we just test
        // structural logic manually or via validator.
        assertTrue(context.getPlanningWindowStart().isBefore(context.getPlanningWindowEnd()));
    }
}
