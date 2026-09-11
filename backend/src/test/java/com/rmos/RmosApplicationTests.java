package com.rmos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.rmos.repository.AssetRepository;
import com.rmos.repository.MaintenanceTaskRepository;
import com.rmos.repository.SourceDataRecordRepository;
import com.rmos.repository.CanonicalAssetMappingRepository;
import com.rmos.repository.decision.PlanDecisionAuditRepository;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class RmosApplicationTests {

    // These repository mocks seamlessly satisfy the constructor dependencies of the
    // Service layer
    // components, effectively isolating the foundational ApplicationContext wiring
    // test from the
    // real yet-to-be-configured PostgreSQL infrastructure interactions.
    @MockBean
    private AssetRepository assetRepository;

    @MockBean
    private MaintenanceTaskRepository maintenanceTaskRepository;

    @MockBean
    private SourceDataRecordRepository sourceDataRecordRepository;

    @MockBean
    private CanonicalAssetMappingRepository canonicalAssetMappingRepository;

    @MockBean
    private PlanDecisionAuditRepository planDecisionAuditRepository;

    @Test
    void contextLoads() {
        // Verifies if the Spring application context can start successfully
    }

}
