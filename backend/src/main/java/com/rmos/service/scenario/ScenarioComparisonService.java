package com.rmos.service.scenario;

import com.rmos.dto.scenario.ScenarioComparisonMetric;
import com.rmos.dto.scenario.ScenarioComparisonRequest;
import com.rmos.dto.scenario.ScenarioComparisonResult;
import com.rmos.dto.scenario.ScenarioResult;
import com.rmos.dto.scenario.ScenarioMetrics;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScenarioComparisonService {

    public ScenarioComparisonResult compare(ScenarioComparisonRequest request) {
        List<ScenarioResult> scenarios = request.getScenarios();
        if (scenarios == null || scenarios.size() < 2) {
            throw new IllegalArgumentException("At least two scenarios required for comparison");
        }

        List<ScenarioResult> validScenarios = scenarios.stream()
                .filter(s -> s.getMetrics() != null)
                .collect(Collectors.toList());

        if (validScenarios.isEmpty()) {
            ScenarioComparisonResult res = new ScenarioComparisonResult();
            res.setScenarios(scenarios);
            res.setExplanation("No valid scenarios with metrics found to compare.");
            return res;
        }

        validScenarios.sort(new ScenarioComparator());
        ScenarioResult best = validScenarios.get(0);

        ScenarioComparisonResult result = new ScenarioComparisonResult();
        result.setScenarios(scenarios);
        result.setBestScenarioId(best.getScenarioId());
        result.setComparisonMetrics(buildComparisonMetrics(validScenarios));
        result.setExplanation(buildExplanation(best));
        return result;
    }

    private String buildExplanation(ScenarioResult best) {
        return "Deterministic comparison evaluated. Best scenario selected: " + best.getScenarioId() +
                ". Basis: 1. Max Scheduled Tasks, 2. Max Priority Sum, 3. Min Unscheduled, " +
                "4. Min Train Impact, 5. Min Workforce Load, 6. Lexicographical ID Tie-break.";
    }

    private List<ScenarioComparisonMetric> buildComparisonMetrics(List<ScenarioResult> sortedScenarios) {
        List<ScenarioComparisonMetric> metrics = new ArrayList<>();
        for (int i = 0; i < sortedScenarios.size(); i++) {
            ScenarioResult s = sortedScenarios.get(i);
            int rank = i + 1;

            ScenarioComparisonMetric m = new ScenarioComparisonMetric();
            m.setScenarioId(s.getScenarioId());
            m.setMetricName("RANK");
            m.setMetricValue((double) rank); // Using rank as a displayable metric metricValue
            m.setRank(rank);
            metrics.add(m);
        }
        return metrics;
    }

    private static class ScenarioComparator implements Comparator<ScenarioResult> {
        @Override
        public int compare(ScenarioResult s1, ScenarioResult s2) {
            ScenarioMetrics m1 = s1.getMetrics();
            ScenarioMetrics m2 = s2.getMetrics();

            // 1. More scheduled tasks (Descending)
            int c = Integer.compare(m2.getScheduledTasks(), m1.getScheduledTasks());
            if (c != 0)
                return c;

            // 2. Higher scheduled priority sum (Descending)
            double p1 = m1.getScheduledPrioritySum() != null ? m1.getScheduledPrioritySum() : 0.0;
            double p2 = m2.getScheduledPrioritySum() != null ? m2.getScheduledPrioritySum() : 0.0;
            c = Double.compare(p2, p1);
            if (c != 0)
                return c;

            // 3. Fewer unscheduled tasks (Ascending)
            c = Integer.compare(m1.getUnscheduledTasks(), m2.getUnscheduledTasks());
            if (c != 0)
                return c;

            // 4. Lower total train impact (Ascending)
            c = Integer.compare(m1.getTrainImpactTotal(), m2.getTrainImpactTotal());
            if (c != 0)
                return c;

            // 5. Lower workforce requirement (Ascending)
            c = Integer.compare(m1.getWorkforceRequirementTotal(), m2.getWorkforceRequirementTotal());
            if (c != 0)
                return c;

            // 6. Earlier deterministic tie-break using scenarioId (Lexicographical
            // Ascending)
            if (s1.getScenarioId() != null && s2.getScenarioId() != null) {
                return s1.getScenarioId().compareTo(s2.getScenarioId());
            }

            return 0; // Both missing IDs
        }
    }
}
