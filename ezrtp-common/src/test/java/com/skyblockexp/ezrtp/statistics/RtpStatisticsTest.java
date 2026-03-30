package com.skyblockexp.ezrtp.statistics;

import org.bukkit.block.Biome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RTP statistics tracking including biome-specific data collection.
 */
class RtpStatisticsTest {

    private RtpStatistics statistics;

    @BeforeEach
    void setUp() {
        statistics = new RtpStatistics();
    }

    @Test
    void testBasicSuccessRecording() {
        statistics.recordAttempt(true, 100L, Biome.PLAINS, false);
        
        assertEquals(1, statistics.getTotalAttempts());
        assertEquals(1, statistics.getTotalSuccesses());
        assertEquals(0, statistics.getTotalFailures());
        assertEquals(100.0, statistics.getSuccessRate());
    }

    @Test
    void testBasicFailureRecording() {
        statistics.recordAttempt(false, 50L, null, false);
        
        assertEquals(1, statistics.getTotalAttempts());
        assertEquals(0, statistics.getTotalSuccesses());
        assertEquals(1, statistics.getTotalFailures());
        assertEquals(0.0, statistics.getSuccessRate());
    }

    @Test
    void testBiomeStatisticsTracking() {
        // Record multiple attempts for different biomes
        statistics.recordAttempt(true, 100L, Biome.PLAINS, false);
        statistics.recordAttempt(true, 150L, Biome.PLAINS, false);
        statistics.recordBiomeFailure(Biome.DESERT);
        statistics.recordAttempt(true, 200L, Biome.FOREST, false);
        
        var biomeStats = statistics.getBiomeStats();
        
        assertNotNull(biomeStats);
        assertEquals(3, biomeStats.size(), "Should track 3 different biomes");
        
        RtpStatistics.BiomeStats plainsStats = biomeStats.get(Biome.PLAINS);
        assertNotNull(plainsStats);
        assertEquals(2, plainsStats.getAttempts());
        assertEquals(2, plainsStats.getSuccesses());
        assertEquals(125.0, plainsStats.getAverageTimeMs());
    }

    @Test
    void testCacheHitTracking() {
        statistics.recordAttempt(true, 50L, Biome.PLAINS, true, true);
        statistics.recordAttempt(true, 100L, Biome.FOREST, false, true);
        
        assertEquals(1, statistics.getTotalCacheHits());
        assertEquals(1, statistics.getTotalCacheMisses());
        assertEquals(50.0, statistics.getCacheHitRate());
    }

    @Test
    void testFailureCauseTracking() {
        statistics.recordSafetyFailure();
        statistics.recordProtectionFailure();
        statistics.recordBiomeFailure(Biome.OCEAN);
        statistics.recordTimeoutFailure();
        statistics.recordEconomyFailure();
        statistics.recordPlayerOfflineOrCancelledFailure();
        statistics.recordTeleportApiFailure();
        statistics.recordGenericSearchErrorFailure();
        
        RtpStatistics.FailureCauses causes = statistics.getFailureCauses();
        
        assertEquals(1, causes.safety());
        assertEquals(1, causes.protection());
        assertEquals(1, causes.biome());
        assertEquals(1, causes.timeout());
        assertEquals(1, causes.economy());
        assertEquals(1, causes.playerOfflineOrCancelled());
        assertEquals(1, causes.teleportApi());
        assertEquals(1, causes.genericSearchError());
        assertEquals(8, causes.total());
    }

    @Test
    void testSuccessRateCalculation() {
        // 3 successes out of 5 attempts = 60%
        statistics.recordAttempt(true, 100L, Biome.PLAINS, false);
        statistics.recordAttempt(false, 50L, null, false);
        statistics.recordAttempt(true, 100L, Biome.PLAINS, false);
        statistics.recordAttempt(false, 50L, null, false);
        statistics.recordAttempt(true, 100L, Biome.PLAINS, false);
        
        assertEquals(5, statistics.getTotalAttempts());
        assertEquals(3, statistics.getTotalSuccesses());
        assertEquals(60.0, statistics.getSuccessRate(), 0.01);
    }

    @Test
    void testAverageTeleportTime() {
        statistics.recordAttempt(true, 100L, Biome.PLAINS, false);
        statistics.recordAttempt(true, 200L, Biome.PLAINS, false);
        statistics.recordAttempt(true, 300L, Biome.PLAINS, false);
        
        assertEquals(200.0, statistics.getAverageTeleportTimeMs(), 0.01);
    }

    @Test
    void testResetStatistics() {
        statistics.recordAttempt(true, 100L, Biome.PLAINS, false);
        statistics.recordBiomeFailure(Biome.DESERT);
        statistics.recordSafetyFailure();
        
        statistics.reset();
        
        assertEquals(0, statistics.getTotalAttempts());
        assertEquals(0, statistics.getTotalSuccesses());
        assertTrue(statistics.getBiomeStats().isEmpty());
        assertEquals(0, statistics.getFailureCauses().total());
    }

    @Test
    void testEmptyStatistics() {
        // No data recorded
        assertEquals(0.0, statistics.getSuccessRate());
        assertEquals(0.0, statistics.getAverageTeleportTimeMs());
        assertEquals(0.0, statistics.getCacheHitRate());
        assertTrue(statistics.getBiomeStats().isEmpty());
    }

    @Test
    void testBiomeStatsSuccessRate() {
        // Test biome-specific success rate calculation
        statistics.recordAttempt(true, 100L, Biome.PLAINS, false);
        statistics.recordAttempt(true, 150L, Biome.PLAINS, false);
        statistics.recordBiomeFailure(Biome.PLAINS);
        
        RtpStatistics.BiomeStats plainsStats = statistics.getBiomeStatsFor(Biome.PLAINS);
        
        assertNotNull(plainsStats);
        assertEquals(3, plainsStats.getAttempts());
        assertEquals(2, plainsStats.getSuccesses());
        assertEquals(1, plainsStats.getFailures());
        assertEquals(66.67, plainsStats.getSuccessRate(), 0.1);
    }

    @Test
    void testBiomeCacheHitTracking() {
        // Test biome-specific cache hit tracking
        statistics.recordAttempt(true, 50L, Biome.PLAINS, true, true);  // Cache hit
        statistics.recordAttempt(true, 100L, Biome.PLAINS, false, true); // Cache miss
        statistics.recordAttempt(true, 75L, Biome.PLAINS, true, true);   // Cache hit
        
        RtpStatistics.BiomeStats plainsStats = statistics.getBiomeStatsFor(Biome.PLAINS);
        
        assertNotNull(plainsStats);
        assertEquals(2, plainsStats.getCacheHits(), "Should have 2 cache hits");
        assertEquals(3, plainsStats.getSuccesses(), "Should have 3 successes");
        assertEquals(3, plainsStats.getAttempts(), "Should have 3 total attempts");
    }

    @Test
    void testBiomeTotalFinds() {
        // Test total finds calculation (successes + cache hits)
        statistics.recordAttempt(true, 100L, Biome.MUSHROOM_FIELDS, false); // New find
        statistics.recordAttempt(true, 50L, Biome.MUSHROOM_FIELDS, true);   // Cache hit
        statistics.recordAttempt(true, 50L, Biome.MUSHROOM_FIELDS, true);   // Cache hit
        statistics.recordAttempt(true, 120L, Biome.MUSHROOM_FIELDS, false); // New find
        
        RtpStatistics.BiomeStats mushroomStats = statistics.getBiomeStatsFor(Biome.MUSHROOM_FIELDS);
        
        assertNotNull(mushroomStats);
        assertEquals(4, mushroomStats.getSuccesses(), "Should have 4 successes");
        assertEquals(2, mushroomStats.getCacheHits(), "Should have 2 cache hits");
        assertEquals(6, mushroomStats.getTotalFinds(), "Total finds should be successes + cache hits");
    }

    @Test
    void testMultipleBiomeCacheHits() {
        // Test cache hits are tracked independently per biome
        statistics.recordAttempt(true, 50L, Biome.PLAINS, true, true);
        statistics.recordAttempt(true, 60L, Biome.FOREST, true, true);
        statistics.recordAttempt(true, 100L, Biome.PLAINS, false, true);
        
        RtpStatistics.BiomeStats plainsStats = statistics.getBiomeStatsFor(Biome.PLAINS);
        RtpStatistics.BiomeStats forestStats = statistics.getBiomeStatsFor(Biome.FOREST);
        
        assertEquals(1, plainsStats.getCacheHits());
        assertEquals(1, forestStats.getCacheHits());
        assertEquals(2, plainsStats.getSuccesses());
        assertEquals(1, forestStats.getSuccesses());
    }

    @Test
    void testBiomeStatsWithOnlyFailures() {
        // Test biome stats when only failures are recorded
        statistics.recordBiomeFailure(Biome.DESERT);
        statistics.recordBiomeFailure(Biome.DESERT);
        statistics.recordBiomeFailure(Biome.DESERT);
        
        RtpStatistics.BiomeStats desertStats = statistics.getBiomeStatsFor(Biome.DESERT);
        
        assertNotNull(desertStats);
        assertEquals(3, desertStats.getAttempts());
        assertEquals(3, desertStats.getFailures());
        assertEquals(0, desertStats.getSuccesses());
        assertEquals(0, desertStats.getCacheHits());
        assertEquals(0, desertStats.getTotalFinds());
        assertEquals(0.0, desertStats.getSuccessRate());
    }

    @Test
    void testCacheMissUsesLookupSemanticsWithNullBiomeFailure() {
        statistics.recordAttempt(false, 50L, null, false, true);
        statistics.recordAttempt(false, 50L, null, false, false);
        statistics.recordAttempt(true, 50L, Biome.PLAINS, true, true);

        assertEquals(1, statistics.getTotalCacheHits());
        assertEquals(1, statistics.getTotalCacheMisses());
        assertEquals(50.0, statistics.getCacheHitRate(), 0.01);
    }
}
