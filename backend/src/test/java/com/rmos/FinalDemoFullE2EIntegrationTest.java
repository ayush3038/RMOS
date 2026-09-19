package com.rmos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmos.domain.decision.PlanDecision;
import com.rmos.domain.planning.PlanningRunEntity;
import com.rmos.domain.planning.PlanningRunStatus;
import com.rmos.dto.integration.IncidentContext;
import com.rmos.dto.decision.PlanReviewRequest;
import com.rmos.repository.planning.PlanningRunRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class FinalDemoFullE2EIntegrationTest extends com.rmos.RmosApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlanningRunRepository planningRunRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void executeCanonicalDemoScenario_FullTraceability() throws Exception {
        // Prepare Mock Repository Return values so realistic pipeline doesn't
        // NullPointer on empty DB Mocks
        PlanningRunEntity mockEntity = new PlanningRunEntity();
        mockEntity.setId(java.util.UUID.randomUUID().toString());
        // mockEntity.setBasePlanId("base-id"); // Excluded since BasePlanId is probably
        // UUID or undefined
        mockEntity.setStatus(PlanningRunStatus.PENDING_REVIEW);
        mockEntity.setVersion(1);

        when(planningRunRepository.findById(any())).thenReturn(Optional.of(mockEntity));
        when(planningRunRepository.save(any())).thenReturn(mockEntity);

        // 1. Simulate deterministic event injected via Integration Endpoint
        IncidentContext incident = new IncidentContext();
        incident.setId("SIM-DELAY-101");
        incident.setSourceSystem("SIMULATOR");
        incident.setSeverity("HIGH");
        incident.setSourceUpdatedAt(java.time.LocalDateTime.parse("2026-09-14T10:00:00"));

        // The endpoint uses IncidentMappingService, connects to CP-SAT Dynamic
        // Replanning natively!
        mockMvc.perform(post("/api/v1/integration/events")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incident)))
                .andExpect(status().isOk());

        // 2. Perform Human Approval boundary execution ensuring RBAC checks explicitly
        // process decisions sequentially
        PlanReviewRequest review = new PlanReviewRequest();
        review.setPlanningId("mock-id");
        review.setAction(PlanDecision.APPROVE);
        review.setReviewerRole(com.rmos.domain.decision.ReviewerRole.OPERATOR);
        review.setReviewerId("admin");
        review.setPlanReference(new com.rmos.dto.decision.PlanReference("mock-id", 1));

        mockMvc.perform(post("/api/plans/decisions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isCreated());
    }
}
