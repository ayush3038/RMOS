package com.rmos.service.planning;

import com.rmos.domain.planning.BlockWindowStatus;
import com.rmos.domain.planning.OperationalConstraintStatus;
import com.rmos.domain.planning.PlanningConstraintType;
import com.rmos.dto.planning.MaintenanceBlockWindow;
import com.rmos.dto.planning.OperationalConstraintEvaluation;
import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.RailwayPlanningContext;
import com.rmos.dto.planning.RailwayPlanningTask;
import com.rmos.dto.planning.WorkforceProfile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RailwayPlanningValidationService {

    public List<OperationalConstraintEvaluation> validateContext(PlanningRequest request) {
        List<OperationalConstraintEvaluation> evaluations = new ArrayList<>();
        RailwayPlanningContext context = request.getPlanningContext();

        if (context == null) {
            return evaluations; // Non-railway planning request, context valid/ignored
        }

        LocalDateTime pStart = context.getPlanningWindowStart();
        LocalDateTime pEnd = context.getPlanningWindowEnd();

        if (pStart == null || pEnd == null || !pStart.isBefore(pEnd)) {
            evaluations.add(createEval(PlanningConstraintType.DATA_VALIDITY, OperationalConstraintStatus.VIOLATED, true,
                    "Invalid planning window bounds in context."));
        }

        if (context.getBlockWindows() != null) {
            for (MaintenanceBlockWindow block : context.getBlockWindows()) {
                if (block.getWindowStart() == null || block.getWindowEnd() == null
                        || !block.getWindowStart().isBefore(block.getWindowEnd())) {
                    evaluations
                            .add(createEval(PlanningConstraintType.DATA_VALIDITY, OperationalConstraintStatus.VIOLATED,
                                    true, "Block window " + block.getBlockId() + " has invalid bounding time."));
                }

                if (block.getWindowStart() != null && pStart != null && block.getWindowStart().isBefore(pStart)) {
                    evaluations
                            .add(createEval(PlanningConstraintType.DATA_VALIDITY, OperationalConstraintStatus.VIOLATED,
                                    true, "Block window " + block.getBlockId() + " starts before planning window."));
                }

                if (block.getWindowEnd() != null && pEnd != null && block.getWindowEnd().isAfter(pEnd)) {
                    evaluations
                            .add(createEval(PlanningConstraintType.DATA_VALIDITY, OperationalConstraintStatus.VIOLATED,
                                    true, "Block window " + block.getBlockId() + " ends after planning window."));
                }
            }
        }

        if (request.getTasks() != null) {
            for (var taskObj : request.getTasks()) {
                if (taskObj instanceof RailwayPlanningTask task) {
                    validateTask(task, context, evaluations);
                }
            }
        }

        return evaluations;
    }

    private void validateTask(RailwayPlanningTask task, RailwayPlanningContext context,
            List<OperationalConstraintEvaluation> evaluations) {
        // Validate Section
        if (task.getSectionId() != null) {
            boolean sectionExists = context.getSections() != null && context.getSections().stream()
                    .anyMatch(s -> task.getSectionId().equals(s.getSectionId()));
            if (!sectionExists) {
                evaluations.add(createEval(PlanningConstraintType.SECTION_AVAILABILITY,
                        OperationalConstraintStatus.VIOLATED, true,
                        "Task " + task.getTaskId() + " refers to an unknown section " + task.getSectionId()));
            }
        }

        // Validate Workforce Profile
        if (task.getWorkforceProfile() != null) {
            WorkforceProfile wp = task.getWorkforceProfile();
            if (wp.getRequiredTeams() != null && wp.getAvailableTeams() != null) {
                if (wp.getRequiredTeams() < 0 || wp.getAvailableTeams() < 0) {
                    evaluations
                            .add(createEval(PlanningConstraintType.DATA_VALIDITY, OperationalConstraintStatus.VIOLATED,
                                    true, "Task " + task.getTaskId() + " has negative workforce parameters."));
                } else if (wp.getRequiredTeams() > wp.getAvailableTeams()) {
                    evaluations.add(createEval(PlanningConstraintType.WORKFORCE_CAPACITY,
                            OperationalConstraintStatus.VIOLATED, false,
                            "Task " + task.getTaskId() + " requires more workforce teams than available."));
                }
            }
        }
    }

    private OperationalConstraintEvaluation createEval(PlanningConstraintType type, OperationalConstraintStatus status,
            boolean hard, String msg) {
        OperationalConstraintEvaluation eval = new OperationalConstraintEvaluation();
        eval.setConstraintType(type);
        eval.setStatus(status);
        eval.setHard(hard);
        eval.setMessage(msg);
        return eval;
    }
}
