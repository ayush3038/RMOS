package com.rmos.service.integration;

import com.rmos.dto.integration.IncidentContext;
import com.rmos.dto.integration.IncidentMappingResult;
import com.rmos.dto.integration.IncidentMappingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentMappingIntegrationTest {

    private IncidentMappingService mappingService;

    @BeforeEach
    void setUp() {
        mappingService = new IncidentMappingService();
    }

    @Test
    void testValidIncidentMapping() {
        IncidentContext idx = new IncidentContext();
        idx.setId("INC-1001");
        idx.setSourceSystem("TMS");
        idx.setSourceRecordId("TMS-200");
        idx.setIncidentType("TRAIN_DELAY");
        idx.setFreshness("FRESH");
        idx.setSourceUpdatedAt(LocalDateTime.now());
        idx.setAssetId(42L);
        idx.setImpactWindowStart(LocalDateTime.now().plusHours(1));
        idx.setImpactWindowEnd(LocalDateTime.now().plusHours(2));

        IncidentMappingResult res = mappingService.mapIncidentToPlanningContext(idx);

        assertThat(res.getStatus()).isEqualTo(IncidentMappingStatus.MAPPED);
        assertThat(res.getContext().getPlanningWindowStart()).isEqualTo(idx.getImpactWindowStart());
    }

    @Test
    void testStaleIncidentRejected() {
        IncidentContext stale = new IncidentContext();
        stale.setSourceSystem("TMS");
        stale.setSourceRecordId("TMS-201");
        stale.setFreshness("STALE");
        stale.setSourceUpdatedAt(LocalDateTime.now().minusDays(2));

        IncidentMappingResult res = mappingService.mapIncidentToPlanningContext(stale);

        assertThat(res.getStatus()).isEqualTo(IncidentMappingStatus.STALE);
        assertThat(res.getContext()).isNull();
    }

    @Test
    void testUnknownAssetRejected() {
        IncidentContext unk = new IncidentContext();
        unk.setSourceSystem("BDMS");
        unk.setSourceRecordId("BDMS-99");
        unk.setSourceUpdatedAt(LocalDateTime.now());
        unk.setFreshness("FRESH");
        // No Asset, Corridor, Train ID set

        IncidentMappingResult res = mappingService.mapIncidentToPlanningContext(unk);

        assertThat(res.getStatus()).isEqualTo(IncidentMappingStatus.UNMAPPED);
    }

    @Test
    void testConflictingTimeBoundariesRejected() {
        IncidentContext conflict = new IncidentContext();
        conflict.setSourceSystem("SMMS");
        conflict.setSourceRecordId("SMMS-333");
        conflict.setSourceUpdatedAt(LocalDateTime.now());
        conflict.setFreshness("FRESH");
        conflict.setAssetId(99L);
        conflict.setImpactWindowStart(LocalDateTime.now().plusHours(5));
        conflict.setImpactWindowEnd(LocalDateTime.now().minusHours(1)); // IMPOSSIBLE

        IncidentMappingResult res = mappingService.mapIncidentToPlanningContext(conflict);

        assertThat(res.getStatus()).isEqualTo(IncidentMappingStatus.DATA_CONFLICT);
    }
}
