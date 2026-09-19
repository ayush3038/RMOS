package com.rmos.service.replanning;

import com.rmos.domain.event.RmosRealtimeEvent;
import com.rmos.domain.planning.PlanAssignmentEntity;
import com.rmos.domain.planning.PlanningDecisionStatus;
import com.rmos.domain.planning.PlanningRunEntity;
import com.rmos.domain.planning.PlanningRunStatus;
import com.rmos.domain.planning.PlanningTaskStatus;
import com.rmos.dto.planning.PlanningRequest;
import com.rmos.dto.planning.PlanningResult;
import com.rmos.dto.planning.PlanningTask;
import com.rmos.repository.planning.PlanAssignmentRepository;
import com.rmos.repository.planning.PlanningRunRepository;
import com.rmos.service.planning.PlanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("test")
class DynamicReplanningIntegrationTest {

    @Autowired
    private DynamicReplanningService replanningService;

    @Autowired
    private PlanningRunRepository runRepository;

    @Autowired
    private PlanAssignmentRepository assignmentRepository;

    @MockBean
    private PlanningService mockPlanningService;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        runRepository.deleteAll();
    }

    private PlanningRunEntity createBaselinePlan() {
        PlanningRunEntity baseline = new PlanningRunEntity();
        baseline.setId(UUID.randomUUID().toString());
        baseline.setVersion(1);
        baseline.setStatus(PlanningRunStatus.CURRENT);
        baseline.setCreatedAt(LocalDateTime.now());

        PlanAssignmentEntity assignment = new PlanAssignmentEntity();
        assignment.setTaskId(101L);
        assignment.setAssignedStart(LocalDateTime.now().plusHours(1));
        assignment.setAssignedEnd(LocalDateTime.now().plusHours(3));
        assignment.setStatus(PlanningTaskStatus.SCHEDULED);
        assignment.setPlanningRun(baseline);

        baseline.setAssignments(List.of(assignment));
        return runRepository.save(baseline);
    }

    private void mockFeasibleOutcome() {
        PlanningResult mockOutput = new PlanningResult();
        mockOutput.setDecisionStatus(PlanningDecisionStatus.FEASIBLE);
        mockOutput.setAssignments(List.of());
        when(mockPlanningService.plan(any())).thenReturn(mockOutput);
    }

    @Test
    void testDuplicateEventsAreIdempotent() {
        PlanningRunEntity baseline = createBaselinePlan();
        mockFeasibleOutcome();

        RmosRealtimeEvent event = new RmosRealtimeEvent();
        event.setEventType("TRAIN_DELAY");
        event.setEntityId("EVT-100");
        event.setSequence(1L);
        event.setPayload("{\"delayMinutes\": 30}");

        PlanningRequest ctx = new PlanningRequest();
        ctx.setPlanningId(baseline.getId());
        PlanningTask task = new PlanningTask();
        task.setTaskId(101L);
        ctx.setTasks(List.of(task));
        ctx.setPlanningWindowStart(LocalDateTime.now());
        ctx.setPlanningWindowEnd(LocalDateTime.now().plusHours(10));

        // First event triggers replan
        var result1 = replanningService.handleRealtimeEvent(event, ctx);
        assertThat(result1).isNotNull();

        // Second identical event should NOT trigger another replan / duplicate
        // candidate
        var result2 = replanningService.handleRealtimeEvent(event, ctx);
        assertThat(result2).isNull(); // Should return null (idempotent / ignored)
    }

    @Test
    void testRapidConcurrentEvents() throws InterruptedException {
        PlanningRunEntity baseline = createBaselinePlan();
        mockFeasibleOutcome();

        RmosRealtimeEvent event1 = new RmosRealtimeEvent();
        event1.setEventType("TRAIN_DELAY");
        event1.setEntityId("EVT-101");
        event1.setPayload("{\"delayMinutes\": 30}");

        RmosRealtimeEvent event2 = new RmosRealtimeEvent();
        event2.setEventType("TRAIN_DELAY");
        event2.setEntityId("EVT-102");
        event2.setPayload("{\"delayMinutes\": 45}");

        PlanningRequest ctx = new PlanningRequest();
        ctx.setPlanningId(baseline.getId());
        ctx.setTasks(List.of(new PlanningTask()));
        ctx.setPlanningWindowStart(LocalDateTime.now());
        ctx.setPlanningWindowEnd(LocalDateTime.now().plusHours(10));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        executor.submit(() -> {
            try {
                replanningService.handleRealtimeEvent(event1, ctx);
            } catch (Exception e) {
            }
            latch.countDown();
        });

        executor.submit(() -> {
            try {
                replanningService.handleRealtimeEvent(event2, ctx);
            } catch (Exception e) {
            }
            latch.countDown();
        });

        latch.await(5, TimeUnit.SECONDS);

        // Only one of these concurrent threads should have succeeded in updating the
        // CURRENT plan to REPLANNING securely!
        long pendingCandidates = runRepository.findAll().stream()
                .filter(r -> r.getStatus() == PlanningRunStatus.PENDING_REVIEW)
                .count();

        // Optimistic locking means only 1 wins natively, preventing double-candidate
        // splits
        assertThat(pendingCandidates).isEqualTo(1);
    }
}
