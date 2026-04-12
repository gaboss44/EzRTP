package com.skyblockexp.ezrtp.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the timing ring buffer, chunk load tracking, and biome rejection
 * counters added to {@link RtpStatistics}.
 */
class RtpStatisticsTimingTest {

    private RtpStatistics statistics;

    @BeforeEach
    void setUp() {
        statistics = new RtpStatistics();
    }

    // ── timing percentiles ────────────────────────────────────────────────────

    @Test
    void testPercentileReturnsZeroWhenNoSamples() {
        assertEquals(0.0, statistics.getTimingPercentile(50));
        assertEquals(0.0, statistics.getTimingPercentile(99));
    }

    @Test
    void testSingleSampleAllPercentilesReturnThatSample() {
        statistics.recordAttempt(true, 42L, null, false);

        assertEquals(42.0, statistics.getTimingPercentile(1));
        assertEquals(42.0, statistics.getTimingPercentile(50));
        assertEquals(42.0, statistics.getTimingPercentile(99));
    }

    @Test
    void testMedianOfOddSortedSamples() {
        // samples: 10, 20, 30, 40, 50  → median = 30
        for (long v : new long[]{50, 10, 40, 20, 30}) {
            statistics.recordAttempt(true, v, null, false);
        }

        assertEquals(30.0, statistics.getTimingPercentile(50));
    }

    @Test
    void testP99WithManyUniformSamples() {
        for (int i = 1; i <= 100; i++) {
            statistics.recordAttempt(true, (long) i, null, false);
        }

        // p99 should be near the 99th value (99 for 100-element set)
        double p99 = statistics.getTimingPercentile(99);
        assertTrue(p99 >= 98.0 && p99 <= 100.0,
                "p99 should be near 99, got " + p99);
    }

    @Test
    void testMinMaxTrackingAfterAttempts() {
        statistics.recordAttempt(true, 100L, null, false);
        statistics.recordAttempt(true, 50L, null, false);
        statistics.recordAttempt(true, 200L, null, false);

        assertEquals(50L, statistics.getMinTimeMs());
        assertEquals(200L, statistics.getMaxTimeMs());
    }

    @Test
    void testMinMaxReturnZeroWhenNoSamples() {
        assertEquals(0L, statistics.getMinTimeMs());
        assertEquals(0L, statistics.getMaxTimeMs());
    }

    @Test
    void testSlowOperationCount() {
        statistics.recordAttempt(true, 50L, null, false);
        statistics.recordAttempt(true, 100L, null, false);
        statistics.recordAttempt(true, 150L, null, false);
        statistics.recordAttempt(true, 200L, null, false);

        // threshold = 100 → samples 150 and 200 are above
        assertEquals(2, statistics.getSlowOperationCount(100L));
        assertEquals(0, statistics.getSlowOperationCount(200L));
    }

    @Test
    void testTimingFieldsClearedOnReset() {
        statistics.recordAttempt(true, 99L, null, false);
        statistics.recordAttempt(true, 101L, null, false);

        statistics.reset();

        assertEquals(0.0, statistics.getTimingPercentile(50));
        assertEquals(0L, statistics.getMinTimeMs());
        assertEquals(0L, statistics.getMaxTimeMs());
        assertEquals(0, statistics.getSlowOperationCount(50L));
    }

    // ── chunk load tracking ───────────────────────────────────────────────────

    @Test
    void testChunkLoadCounters() {
        statistics.recordChunkLoad(true);
        statistics.recordChunkLoad(true);
        statistics.recordChunkLoad(false);

        assertEquals(3L, statistics.getTotalChunkLoads());
        assertEquals(2L, statistics.getAsyncChunkLoads());
        assertEquals(1L, statistics.getSyncChunkLoads());
    }

    @Test
    void testChunkLoadCountersClearedOnReset() {
        statistics.recordChunkLoad(true);
        statistics.reset();

        assertEquals(0L, statistics.getTotalChunkLoads());
        assertEquals(0L, statistics.getAsyncChunkLoads());
        assertEquals(0L, statistics.getSyncChunkLoads());
    }

    // ── biome rejection tracking ──────────────────────────────────────────────

    @Test
    void testBiomeRejectionMaxUpdates() {
        statistics.recordBiomeRejectionCount(5);
        statistics.recordBiomeRejectionCount(3);
        statistics.recordBiomeRejectionCount(8);
        statistics.recordBiomeRejectionCount(2);

        assertEquals(8, statistics.getMaxBiomeRejectionsPerRtp());
    }

    @Test
    void testBiomeRejectionCountZeroIsIgnored() {
        statistics.recordBiomeRejectionCount(0);

        assertEquals(0, statistics.getMaxBiomeRejectionsPerRtp());
    }

    @Test
    void testBiomeFilterTimeoutCounter() {
        statistics.recordBiomeFilterTimeout();
        statistics.recordBiomeFilterTimeout();

        assertEquals(2, statistics.getBiomeFilterTimeouts());
    }

    @Test
    void testBiomeRejectionCountersClearedOnReset() {
        statistics.recordBiomeRejectionCount(10);
        statistics.recordBiomeFilterTimeout();
        statistics.reset();

        assertEquals(0, statistics.getMaxBiomeRejectionsPerRtp());
        assertEquals(0, statistics.getBiomeFilterTimeouts());
        assertEquals(0L, statistics.getTotalBiomeRejections());
    }
}
