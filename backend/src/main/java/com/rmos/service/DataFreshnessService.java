package com.rmos.service;

import com.rmos.domain.DataFreshness;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class DataFreshnessService {

    @Value("${rmos.integration.data-freshness.threshold-minutes:60}")
    private long thresholdMinutes;

    public DataFreshness determineFreshness(LocalDateTime timestamp) {
        if (timestamp == null) {
            return DataFreshness.UNKNOWN;
        }

        LocalDateTime now = LocalDateTime.now();
        if (timestamp.isAfter(now)) {
            // Future timestamps are considered unknown/invalid conceptually, but we can
            // return UNKNOWN or handle specifically.
            // Requirement specifies generic FRESH/STALE/UNKNOWN deterministic checking
            // based on elapsed time.
            return DataFreshness.UNKNOWN;
        }

        long minutesElapsed = ChronoUnit.MINUTES.between(timestamp, now);
        if (minutesElapsed <= thresholdMinutes) {
            return DataFreshness.FRESH;
        } else {
            return DataFreshness.STALE;
        }
    }
}
