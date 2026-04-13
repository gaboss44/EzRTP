package com.skyblockexp.ezrtp.config.teleport;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import com.skyblockexp.ezrtp.config.teleport.ChunkLoadingSettings;

class ChunkLoadingSettingsTest {

    @Test
    void testDefaults() {
        ChunkLoadingSettings settings = ChunkLoadingSettings.defaults();

        assertTrue(settings.isEnabled());
        assertEquals(3L, settings.getProcessingIntervalTicks());
        assertEquals(2, settings.getMaxChunksPerTick());
    }

    @Test
    void testFromConfiguration() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("enabled", false);
        config.set("interval-ticks", 10);
        config.set("max-chunks-per-tick", 3);

        ChunkLoadingSettings settings = ChunkLoadingSettings.fromConfiguration(config, ChunkLoadingSettings.defaults());

        assertFalse(settings.isEnabled());
        assertEquals(10L, settings.getProcessingIntervalTicks());
        assertEquals(3, settings.getMaxChunksPerTick());
    }

    @Test
    void testFromConfigurationNullUsesFallback() {
        ChunkLoadingSettings fallback = new ChunkLoadingSettings(false, 6L, 2);
        ChunkLoadingSettings settings = ChunkLoadingSettings.fromConfiguration(null, fallback);

        assertFalse(settings.isEnabled());
        assertEquals(6L, settings.getProcessingIntervalTicks());
        assertEquals(2, settings.getMaxChunksPerTick());
    }
}
