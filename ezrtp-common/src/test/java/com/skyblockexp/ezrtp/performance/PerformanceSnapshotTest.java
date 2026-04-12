package com.skyblockexp.ezrtp.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceSnapshotTest {

    private static PerformanceSnapshot buildSnapshot(
            long totalRtps,
            long successRtps,
            long failureRtps,
            double successRate,
            double p50, double p75, double p90, double p95, double p99,
            long minMs, long maxMs,
            long slowOps,
            double cacheHitRate,
            long cacheHits, long cacheMisses, long cacheEvictions,
            long chunksTotal, long chunksAsync, long chunksSync,
            long biomeRejTotal, long biomeRejMax, long biomeFilterTimeouts) {
        return new PerformanceSnapshot(
                "2024-01-01T00:00:00Z",
                totalRtps, successRtps, failureRtps, successRate,
                p50, p75, p90, p95, p99,
                minMs, maxMs, slowOps,
                cacheHitRate, cacheHits, cacheMisses, cacheEvictions,
                chunksTotal, chunksAsync, chunksSync,
                biomeRejTotal, biomeRejMax, biomeFilterTimeouts);
    }

    @Test
    void testGettersReturnConstructedValues() {
        PerformanceSnapshot snap = buildSnapshot(
                100, 90, 10, 90.0,
                50.0, 62.0, 91.0, 120.0, 250.0,
                12L, 890L,
                5,
                72.3, 830, 320, 150,
                3421, 3100, 321,
                8901, 47, 12);

        assertEquals(100, snap.getTotalRtps());
        assertEquals(90, snap.getSuccessRtps());
        assertEquals(10, snap.getFailureRtps());
        assertEquals(90.0, snap.getSuccessRatePercent());
        assertEquals(50.0, snap.getTimingP50Ms());
        assertEquals(250.0, snap.getTimingP99Ms());
        assertEquals(12L, snap.getTimingMinMs());
        assertEquals(890L, snap.getTimingMaxMs());
        assertEquals(5, snap.getSlowOperations());
        assertEquals(72.3, snap.getCacheHitRatePercent());
        assertEquals(830, snap.getCacheHits());
        assertEquals(320, snap.getCacheMisses());
        assertEquals(150, snap.getCacheEvictions());
        assertEquals(3421, snap.getChunkLoadsTotal());
        assertEquals(3100, snap.getChunkLoadsAsync());
        assertEquals(321, snap.getChunkLoadsSync());
        assertEquals(8901, snap.getBiomeRejectionsTotal());
        assertEquals(47, snap.getBiomeRejectionsMaxPerRtp());
        assertEquals(12, snap.getBiomeFilterTimeouts());
    }

    @Test
    void testToJsonContainsExpectedKeys() {
        PerformanceSnapshot snap = buildSnapshot(
                10, 9, 1, 90.0,
                10.0, 20.0, 30.0, 40.0, 50.0,
                5L, 100L,
                0,
                80.0, 8, 2, 1,
                5, 4, 1,
                3, 2, 0);

        String json = snap.toJson();

        assertTrue(json.contains("\"generatedAt\""), "should have generatedAt");
        assertTrue(json.contains("\"rtp\""), "should have rtp section");
        assertTrue(json.contains("\"timing\""), "should have timing section");
        assertTrue(json.contains("\"cache\""), "should have cache section");
        assertTrue(json.contains("\"chunkLoading\""), "should have chunkLoading section");
        assertTrue(json.contains("\"biomeFiltering\""), "should have biomeFiltering section");
        assertTrue(json.contains("\"total\": 10"), "should show total RTPs");
        assertTrue(json.contains("\"success\": 9"), "should show success RTPs");
    }

    @Test
    void testToJsonIsValidStructure() {
        PerformanceSnapshot snap = buildSnapshot(
                0, 0, 0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0,
                0L, 0L,
                0,
                0.0, 0, 0, 0,
                0, 0, 0,
                0, 0, 0);

        String json = snap.toJson();

        // Basic JSON structure checks
        assertTrue(json.startsWith("{"), "should start with {");
        assertTrue(json.endsWith("}"), "should end with }");
        // No trailing commas before closing brace (common JSON mistake)
        assertFalse(json.contains(",\n}"), "should not have trailing commas");
        assertFalse(json.contains(",\n  }"), "should not have trailing commas in nested objects");
    }

    @Test
    void testToJsonFormatsDoublesWith2DecimalPlaces() {
        PerformanceSnapshot snap = buildSnapshot(
                1, 1, 0, 100.0,
                33.333, 66.666, 75.0, 90.0, 95.0,
                10L, 50L,
                0,
                0.0, 0, 0, 0,
                0, 0, 0,
                0, 0, 0);

        String json = snap.toJson();

        // Should not contain raw double values with many decimals
        assertFalse(json.contains("33.333"), "should format to 2dp");
        assertTrue(json.contains("33.33"), "should have 2dp value");
    }

    @Test
    void testGeneratedAtIsPresentInJson() {
        PerformanceSnapshot snap = buildSnapshot(
                0, 0, 0, 0.0,
                0, 0, 0, 0, 0,
                0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0,
                0, 0, 0);

        assertTrue(snap.toJson().contains("2024-01-01T00:00:00Z"));
    }
}
