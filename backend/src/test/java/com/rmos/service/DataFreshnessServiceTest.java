package com.rmos.service;

import com.rmos.domain.DataFreshness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataFreshnessServiceTest {

    private DataFreshnessService service;

    @BeforeEach
    void setUp() {
        service = new DataFreshnessService();
        ReflectionTestUtils.setField(service, "thresholdMinutes", 60L);
    }

    @Test
    void determineFreshness_NullTimestamp_ReturnsUnknown() {
        assertEquals(DataFreshness.UNKNOWN, service.determineFreshness(null));
    }

    @Test
    void determineFreshness_FutureTimestamp_ReturnsUnknown() {
        LocalDateTime future = LocalDateTime.now().plusMinutes(10);
        assertEquals(DataFreshness.UNKNOWN, service.determineFreshness(future));
    }

    @Test
    void determineFreshness_WithinThreshold_ReturnsFresh() {
        LocalDateTime freshTime = LocalDateTime.now().minusMinutes(30);
        assertEquals(DataFreshness.FRESH, service.determineFreshness(freshTime));

        LocalDateTime exactlyThreshold = LocalDateTime.now().minusMinutes(60).plusSeconds(1);
        assertEquals(DataFreshness.FRESH, service.determineFreshness(exactlyThreshold));
    }

    @Test
    void determineFreshness_ExceedsThreshold_ReturnsStale() {
        LocalDateTime staleTime = LocalDateTime.now().minusMinutes(61);
        assertEquals(DataFreshness.STALE, service.determineFreshness(staleTime));
    }
}
