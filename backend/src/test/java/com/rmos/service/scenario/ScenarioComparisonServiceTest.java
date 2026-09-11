package com.rmos.service.scenario;

import com.rmos.dto.scenario.ScenarioComparisonRequest;
import com.rmos.dto.scenario.ScenarioComparisonResult;
import com.rmos.dto.scenario.ScenarioMetrics;
import com.rmos.dto.scenario.ScenarioResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScenarioComparisonServiceTest {

    private ScenarioComparisonService cmpService;

    @BeforeEach
    void setUp() {
        cmpService = new ScenarioComparisonService();
    }

    private ScenarioResult buildBaseResult(String id, int schedTasks, double prio, int unsched, int trainImpact,
            int workforce) {
        ScenarioResult r = new ScenarioResult();
        r.setScenarioId(id);
        ScenarioMetrics m = new ScenarioMetrics();
        m.setScheduledTasks(schedTasks);
        m.setScheduledPrioritySum(prio);
        m.setUnscheduledTasks(unsched);
        m.setTrainImpactTotal(trainImpact);
        m.setWorkforceRequirementTotal(workforce);
        r.setMetrics(m);
        return r;
    }

    @Test
    void testMoreScheduledTasksWins() {
        ScenarioResult s1 = buildBaseResult("S1", 10, 5.0, 2, 0, 0);
        ScenarioResult s2 = buildBaseResult("S2", 12, 5.0, 2, 0, 0);

        ScenarioComparisonRequest req = new ScenarioComparisonRequest();
        req.setScenarios(Arrays.asList(s1, s2));

        ScenarioComparisonResult result = cmpService.compare(req);
        assertEquals("S2", result.getBestScenarioId());
    }

    @Test
    void testEqualScheduledTask_HigherPriorityWins() {
        ScenarioResult s1 = buildBaseResult("S1", 10, 6.0, 2, 0, 0);
        ScenarioResult s2 = buildBaseResult("S2", 10, 5.0, 2, 0, 0);

        ScenarioComparisonRequest req = new ScenarioComparisonRequest();
        req.setScenarios(Arrays.asList(s1, s2));

        ScenarioComparisonResult result = cmpService.compare(req);
        assertEquals("S1", result.getBestScenarioId());
    }

    @Test
    void testEqualPriority_FewerUnscheduledWins() {
        ScenarioResult s1 = buildBaseResult("S1", 10, 6.0, 4, 0, 0);
        ScenarioResult s2 = buildBaseResult("S2", 10, 6.0, 2, 0, 0);

        ScenarioComparisonRequest req = new ScenarioComparisonRequest();
        req.setScenarios(Arrays.asList(s1, s2));

        ScenarioComparisonResult result = cmpService.compare(req);
        assertEquals("S2", result.getBestScenarioId());
    }

    @Test
    void testTieBreak_ScenId() {
        ScenarioResult s1 = buildBaseResult("S_B", 10, 6.0, 2, 0, 0);
        ScenarioResult s2 = buildBaseResult("S_A", 10, 6.0, 2, 0, 0); // lexicographically smaller

        ScenarioComparisonRequest req = new ScenarioComparisonRequest();
        req.setScenarios(Arrays.asList(s1, s2));

        ScenarioComparisonResult result = cmpService.compare(req);
        assertEquals("S_A", result.getBestScenarioId());
    }
}
