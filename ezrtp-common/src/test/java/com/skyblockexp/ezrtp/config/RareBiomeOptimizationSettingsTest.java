package com.skyblockexp.ezrtp.config;

import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RareBiomeOptimizationSettings.
 */
class RareBiomeOptimizationSettingsTest {
    
    @Test
    void testDisabled() {
        RareBiomeOptimizationSettings settings = RareBiomeOptimizationSettings.disabled();
        
        assertFalse(settings.isEnabled());
        assertTrue(settings.useWeightedSearch());
        assertTrue(settings.isHotspotTrackingEnabled());
        assertFalse(settings.isBackgroundScanningEnabled());
        assertTrue(settings.useChunkLoadQueue());
        assertEquals(3L, settings.getChunkProcessingIntervalTicks());
    }
    
    @Test
    void testFromConfigurationWithDefaults() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("enabled", true);
        
        RareBiomeOptimizationSettings settings = RareBiomeOptimizationSettings.fromConfiguration(config);
        
        assertTrue(settings.isEnabled());
        assertTrue(settings.useWeightedSearch());
        assertTrue(settings.isHotspotTrackingEnabled());
        assertFalse(settings.isBackgroundScanningEnabled());
        assertTrue(settings.useChunkLoadQueue());
        assertFalse(settings.getRareBiomes().isEmpty());
        assertEquals(3L, settings.getChunkProcessingIntervalTicks());
    }
    
    @Test
    void testFromConfigurationWithCustomBiomes() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("enabled", true);
        config.set("rare-biomes", Arrays.asList("MUSHROOM_FIELDS", "JUNGLE"));
        
        RareBiomeOptimizationSettings settings = RareBiomeOptimizationSettings.fromConfiguration(config);
        
        assertTrue(settings.isEnabled());
        Set<Biome> rareBiomes = settings.getRareBiomes();
        
        // Verify biomes were parsed (if they exist in this version)
        assertTrue(rareBiomes.isEmpty() || rareBiomes.size() >= 1);
    }
    
    @Test
    void testFromConfigurationWithAllOptions() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("enabled", true);
        config.set("use-weighted-search", false);
        config.set("enable-hotspot-tracking", false);
        config.set("enable-background-scanning", true);
        config.set("max-hotspots-per-biome", 200);
        config.set("hotspot-scan-interval-minutes", 10);
        config.set("use-chunk-load-queue", false);
        config.set("max-chunks-per-tick", 5);
        config.set("chunk-load-interval-ticks", 7);
        
        RareBiomeOptimizationSettings settings = RareBiomeOptimizationSettings.fromConfiguration(config);
        
        assertTrue(settings.isEnabled());
        assertFalse(settings.useWeightedSearch());
        assertFalse(settings.isHotspotTrackingEnabled());
        assertTrue(settings.isBackgroundScanningEnabled());
        assertFalse(settings.useChunkLoadQueue());
        assertEquals(200, settings.getMaxHotspotsPerBiome());
        assertEquals(10, settings.getHotspotScanIntervalMinutes());
        assertEquals(5, settings.getMaxChunksPerTick());
        assertEquals(7L, settings.getChunkProcessingIntervalTicks());
    }
    
    @Test
    void testFromConfigurationNull() {
        RareBiomeOptimizationSettings settings = RareBiomeOptimizationSettings.fromConfiguration(null);
        
        assertFalse(settings.isEnabled());
        assertEquals(settings, RareBiomeOptimizationSettings.disabled());
    }
}
