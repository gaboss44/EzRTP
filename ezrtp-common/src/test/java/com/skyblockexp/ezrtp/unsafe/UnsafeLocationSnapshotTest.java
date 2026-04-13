package com.skyblockexp.ezrtp.unsafe;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UnsafeLocationSnapshotTest {

    private static Map<UnsafeLocationCause, Long> emptyCauses() {
        Map<UnsafeLocationCause, Long> map = new EnumMap<>(UnsafeLocationCause.class);
        for (UnsafeLocationCause cause : UnsafeLocationCause.values()) {
            map.put(cause, 0L);
        }
        return map;
    }

    @Test
    void testToJsonContainsExpectedTopLevelKeys() {
        UnsafeLocationSnapshot snap = new UnsafeLocationSnapshot(
                "2025-01-01T00:00:00Z", 5, 100L, 15L, 0.15, emptyCauses());

        String json = snap.toJson();

        assertTrue(json.contains("\"timestamp\""));
        assertTrue(json.contains("\"period_minutes\""));
        assertTrue(json.contains("\"rtp_operations\""));
        assertTrue(json.contains("\"total_attempts\""));
        assertTrue(json.contains("\"unsafe_attempts\""));
        assertTrue(json.contains("\"unsafe_rate\""));
        assertTrue(json.contains("\"unsafe_causes\""));
        assertTrue(json.contains("\"recommendations\""));
    }

    @Test
    void testToJsonContainsAllCauseKeys() {
        UnsafeLocationSnapshot snap = new UnsafeLocationSnapshot(
                "2025-01-01T00:00:00Z", 5, 100L, 0L, 0.0, emptyCauses());

        String json = snap.toJson();

        for (UnsafeLocationCause cause : UnsafeLocationCause.values()) {
            assertTrue(json.contains("\"" + cause.name().toLowerCase() + "\""),
                    "JSON missing cause key: " + cause.name().toLowerCase());
        }
    }

    @Test
    void testToJsonContainsCorrectValues() {
        Map<UnsafeLocationCause, Long> causes = emptyCauses();
        causes.put(UnsafeLocationCause.VOID, 3L);
        causes.put(UnsafeLocationCause.LAVA, 1L);

        UnsafeLocationSnapshot snap = new UnsafeLocationSnapshot(
                "2025-06-01T12:00:00Z", 5, 200L, 4L, 0.02, causes);

        String json = snap.toJson();

        assertTrue(json.contains("\"2025-06-01T12:00:00Z\""));
        assertTrue(json.contains("200"));
        assertTrue(json.contains("4"));
    }

    @Test
    void testNoRecommendationsWhenNoRejections() {
        UnsafeLocationSnapshot snap = new UnsafeLocationSnapshot(
                "2025-01-01T00:00:00Z", 5, 100L, 0L, 0.0, emptyCauses());

        assertTrue(snap.getRecommendations().isEmpty());
    }

    @Test
    void testRecommendationFiredAtThreshold() {
        // VOID at 100% → should fire recommendation
        Map<UnsafeLocationCause, Long> causes = emptyCauses();
        causes.put(UnsafeLocationCause.VOID, 10L);

        UnsafeLocationSnapshot snap = new UnsafeLocationSnapshot(
                "2025-01-01T00:00:00Z", 5, 100L, 10L, 0.10, causes);

        List<String> recs = snap.getRecommendations();
        assertFalse(recs.isEmpty());
        assertTrue(recs.stream().anyMatch(r -> r.toLowerCase().contains("void")));
    }

    @Test
    void testRecommendationNotFiredBelowThreshold() {
        // VOID at 29% (below 30% threshold) → no recommendation
        Map<UnsafeLocationCause, Long> causes = emptyCauses();
        causes.put(UnsafeLocationCause.VOID, 29L);
        causes.put(UnsafeLocationCause.LAVA, 71L);

        UnsafeLocationSnapshot snap = new UnsafeLocationSnapshot(
                "2025-01-01T00:00:00Z", 5, 200L, 100L, 0.5, causes);

        List<String> recs = snap.getRecommendations();
        // Only LAVA should trigger (71 / 100 = 71% > 30%)
        assertTrue(recs.stream().noneMatch(r -> r.toLowerCase().contains("void")));
        assertTrue(recs.stream().anyMatch(r -> r.toLowerCase().contains("lava")));
    }

    @Test
    void testLiquidRecommendation() {
        Map<UnsafeLocationCause, Long> causes = emptyCauses();
        causes.put(UnsafeLocationCause.LIQUID, 10L);

        UnsafeLocationSnapshot snap = new UnsafeLocationSnapshot(
                "2025-01-01T00:00:00Z", 5, 100L, 10L, 0.10, causes);

        assertTrue(snap.getRecommendations().stream().anyMatch(r -> r.toLowerCase().contains("liquid")));
    }

    @Test
    void testAccessors() {
        Map<UnsafeLocationCause, Long> causes = emptyCauses();
        UnsafeLocationSnapshot snap = new UnsafeLocationSnapshot(
                "ts", 10, 500L, 50L, 0.1, causes);

        assertEquals("ts", snap.getTimestamp());
        assertEquals(10, snap.getPeriodMinutes());
        assertEquals(500L, snap.getTotalAttempts());
        assertEquals(50L, snap.getUnsafeAttempts());
        assertEquals(0.1, snap.getUnsafeRate(), 0.001);
    }
}
