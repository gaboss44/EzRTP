package com.skyblockexp.ezrtp.unsafe;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UnsafeLocationStatisticsTest {

    @Test
    void testInitialCountsAreZero() {
        UnsafeLocationStatistics stats = new UnsafeLocationStatistics();

        Map<UnsafeLocationCause, Long> allTime = stats.getAllTimeCounts();
        for (UnsafeLocationCause cause : UnsafeLocationCause.values()) {
            assertEquals(0L, allTime.get(cause));
        }
        assertEquals(0L, stats.getAllTimeTotal());
    }

    @Test
    void testRecordRejectionIncrementsAllTimeCounts() {
        UnsafeLocationStatistics stats = new UnsafeLocationStatistics();

        stats.recordRejection(UnsafeLocationCause.VOID);
        stats.recordRejection(UnsafeLocationCause.VOID);
        stats.recordRejection(UnsafeLocationCause.LAVA);

        Map<UnsafeLocationCause, Long> allTime = stats.getAllTimeCounts();
        assertEquals(2L, allTime.get(UnsafeLocationCause.VOID));
        assertEquals(1L, allTime.get(UnsafeLocationCause.LAVA));
        assertEquals(3L, stats.getAllTimeTotal());
    }

    @Test
    void testRecordRejectionNullFallsToOther() {
        UnsafeLocationStatistics stats = new UnsafeLocationStatistics();

        stats.recordRejection(null);

        assertEquals(1L, stats.getAllTimeCounts().get(UnsafeLocationCause.OTHER));
        assertEquals(1L, stats.getAllTimeTotal());
    }

    @Test
    void testSnapshotWindowCapturesWindowCounts() {
        UnsafeLocationStatistics stats = new UnsafeLocationStatistics();

        stats.recordRejection(UnsafeLocationCause.LIQUID);
        stats.recordRejection(UnsafeLocationCause.LIQUID);
        stats.recordRejection(UnsafeLocationCause.UNSAFE_BLOCK);

        UnsafeLocationStatistics.WindowSnapshot snap = stats.snapshotWindow(100L, false);

        assertEquals(2L, snap.counts().get(UnsafeLocationCause.LIQUID));
        assertEquals(1L, snap.counts().get(UnsafeLocationCause.UNSAFE_BLOCK));
        assertEquals(3L, snap.totalUnsafe());
        assertEquals(100L, snap.windowAttempts());
    }

    @Test
    void testSnapshotWindowWithResetZeroesWindowCounters() {
        UnsafeLocationStatistics stats = new UnsafeLocationStatistics();

        stats.recordRejection(UnsafeLocationCause.VOID);
        stats.recordRejection(UnsafeLocationCause.VOID);

        UnsafeLocationStatistics.WindowSnapshot beforeReset = stats.snapshotWindow(50L, true);

        // After reset, the window counters should be zero
        UnsafeLocationStatistics.WindowSnapshot afterReset = stats.snapshotWindow(50L, false);

        assertEquals(2L, beforeReset.counts().get(UnsafeLocationCause.VOID));
        assertEquals(0L, afterReset.counts().get(UnsafeLocationCause.VOID));
        assertEquals(0L, afterReset.totalUnsafe());
    }

    @Test
    void testResetDoesNotClearAllTimeCounters() {
        UnsafeLocationStatistics stats = new UnsafeLocationStatistics();

        stats.recordRejection(UnsafeLocationCause.LAVA);
        stats.snapshotWindow(10L, true);

        assertEquals(1L, stats.getAllTimeCounts().get(UnsafeLocationCause.LAVA));
        assertEquals(1L, stats.getAllTimeTotal());
    }

    @Test
    void testUnsafeRateCalculation() {
        UnsafeLocationStatistics stats = new UnsafeLocationStatistics();

        stats.recordRejection(UnsafeLocationCause.VOID);

        UnsafeLocationStatistics.WindowSnapshot snap = stats.snapshotWindow(4L, false);

        // 1 unsafe out of 4 attempts (delta = 4 - 0 = 4)
        assertEquals(0.25, snap.unsafeRate(), 0.001);
    }

    @Test
    void testUnsafeRateZeroWhenNoAttempts() {
        UnsafeLocationStatistics stats = new UnsafeLocationStatistics();
        stats.recordRejection(UnsafeLocationCause.VOID);

        UnsafeLocationStatistics.WindowSnapshot snap = stats.snapshotWindow(0L, false);

        assertEquals(0.0, snap.unsafeRate(), 0.0);
    }

    @Test
    void testWindowAttemptsDeltaAfterReset() {
        UnsafeLocationStatistics stats = new UnsafeLocationStatistics();

        // Simulate 10 RTPs happened; take snapshot with reset (baseline becomes 10)
        stats.snapshotWindow(10L, true);

        // Simulate 5 more RTPs
        stats.recordRejection(UnsafeLocationCause.OTHER);
        UnsafeLocationStatistics.WindowSnapshot snap = stats.snapshotWindow(15L, false);

        assertEquals(5L, snap.windowAttempts());
    }
}
