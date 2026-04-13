package com.skyblockexp.ezrtp.config.teleport;

import com.skyblockexp.ezrtp.config.teleport.ChunkLoadingSettings.LegacyThrottleSettings;
import com.skyblockexp.ezrtp.config.teleport.ChunkLoadingSettings.PaperAsyncApiMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ChunkLoadingSettingsTest {

    // ── defaults ──────────────────────────────────────────────────────────────

    @Test
    void testDefaults() {
        ChunkLoadingSettings settings = ChunkLoadingSettings.defaults();

        assertEquals(PaperAsyncApiMode.AUTO_DETECT, settings.getUsePaperAsyncApi());
        LegacyThrottleSettings legacy = settings.getLegacyThrottle();
        assertTrue(legacy.isEnabled());
        assertEquals(3L, legacy.getProcessingIntervalTicks());
        assertEquals(2, legacy.getMaxChunksPerTick());
    }

    @Test
    void testDefaultsLegacyAccessorsMatch() {
        ChunkLoadingSettings settings = ChunkLoadingSettings.defaults();
        // @Deprecated accessors must delegate to legacyThrottle
        assertTrue(settings.isEnabled());
        assertEquals(3L, settings.getProcessingIntervalTicks());
        assertEquals(2, settings.getMaxChunksPerTick());
    }

    // ── new YAML format ───────────────────────────────────────────────────────

    @Test
    void testFromConfigurationNewFormat() {
        YamlConfiguration root = new YamlConfiguration();
        root.set("use-paper-async-api", "auto-detect");
        root.createSection("legacy-throttle");
        root.set("legacy-throttle.enabled", false);
        root.set("legacy-throttle.interval-ticks", 8);
        root.set("legacy-throttle.max-chunks-per-tick", 4);

        ChunkLoadingSettings settings = ChunkLoadingSettings.fromConfiguration(root, ChunkLoadingSettings.defaults());

        assertEquals(PaperAsyncApiMode.AUTO_DETECT, settings.getUsePaperAsyncApi());
        assertFalse(settings.getLegacyThrottle().isEnabled());
        assertEquals(8L, settings.getLegacyThrottle().getProcessingIntervalTicks());
        assertEquals(4, settings.getLegacyThrottle().getMaxChunksPerTick());
    }

    @Test
    void testFromConfigurationNewFormatAlways() {
        YamlConfiguration root = new YamlConfiguration();
        root.set("use-paper-async-api", "always");
        root.createSection("legacy-throttle");
        root.set("legacy-throttle.enabled", true);
        root.set("legacy-throttle.interval-ticks", 5);
        root.set("legacy-throttle.max-chunks-per-tick", 2);

        ChunkLoadingSettings settings = ChunkLoadingSettings.fromConfiguration(root, ChunkLoadingSettings.defaults());

        assertEquals(PaperAsyncApiMode.ALWAYS, settings.getUsePaperAsyncApi());
        assertTrue(settings.getLegacyThrottle().isEnabled());
    }

    @Test
    void testFromConfigurationNewFormatNever() {
        YamlConfiguration root = new YamlConfiguration();
        root.set("use-paper-async-api", "never");

        ChunkLoadingSettings settings = ChunkLoadingSettings.fromConfiguration(root, ChunkLoadingSettings.defaults());

        assertEquals(PaperAsyncApiMode.NEVER, settings.getUsePaperAsyncApi());
    }

    // ── mode string aliases ────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({"auto-detect,AUTO_DETECT", "auto,AUTO_DETECT", "always,ALWAYS", "true,ALWAYS", "never,NEVER", "false,NEVER"})
    void testModeStringParsing(String input, String expectedName) {
        PaperAsyncApiMode expected = PaperAsyncApiMode.valueOf(expectedName);
        assertEquals(expected, PaperAsyncApiMode.fromString(input, PaperAsyncApiMode.AUTO_DETECT));
    }

    @Test
    void testModeStringNullReturnsDefault() {
        assertEquals(PaperAsyncApiMode.ALWAYS, PaperAsyncApiMode.fromString(null, PaperAsyncApiMode.ALWAYS));
    }

    @Test
    void testModeStringUnknownReturnsDefault() {
        assertEquals(PaperAsyncApiMode.NEVER, PaperAsyncApiMode.fromString("bogus-value", PaperAsyncApiMode.NEVER));
    }

    // ── legacy flat format (backward compat) ──────────────────────────────────

    @Test
    void testFromConfigurationOldFlatFormatMigratesTransparently() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("enabled", false);
        config.set("interval-ticks", 10);
        config.set("max-chunks-per-tick", 3);

        ChunkLoadingSettings settings = ChunkLoadingSettings.fromConfiguration(config, ChunkLoadingSettings.defaults());

        // Mode defaults to AUTO_DETECT when using old flat format
        assertEquals(PaperAsyncApiMode.AUTO_DETECT, settings.getUsePaperAsyncApi());
        assertFalse(settings.getLegacyThrottle().isEnabled());
        assertEquals(10L, settings.getLegacyThrottle().getProcessingIntervalTicks());
        assertEquals(3, settings.getLegacyThrottle().getMaxChunksPerTick());
    }

    // ── null section fallback ─────────────────────────────────────────────────

    @Test
    void testFromConfigurationNullSectionUsesFallback() {
        ChunkLoadingSettings fallback = new ChunkLoadingSettings(false, 6L, 2);
        ChunkLoadingSettings settings = ChunkLoadingSettings.fromConfiguration(null, fallback);

        assertFalse(settings.getLegacyThrottle().isEnabled());
        assertEquals(6L, settings.getLegacyThrottle().getProcessingIntervalTicks());
        assertEquals(2, settings.getLegacyThrottle().getMaxChunksPerTick());
    }

    @Test
    void testFromConfigurationNullSectionNullFallbackUsesStaticDefaults() {
        ChunkLoadingSettings settings = ChunkLoadingSettings.fromConfiguration(null, null);

        assertEquals(PaperAsyncApiMode.AUTO_DETECT, settings.getUsePaperAsyncApi());
        assertTrue(settings.getLegacyThrottle().isEnabled());
    }

    // ── LegacyThrottleSettings.defaults() ────────────────────────────────────

    @Test
    void testLegacyThrottleDefaults() {
        LegacyThrottleSettings defaults = LegacyThrottleSettings.defaults();
        assertTrue(defaults.isEnabled());
        assertEquals(3L, defaults.getProcessingIntervalTicks());
        assertEquals(2, defaults.getMaxChunksPerTick());
    }

    @Test
    void testLegacyThrottleIntervalClamped() {
        LegacyThrottleSettings settings = new LegacyThrottleSettings(true, 0L, 1);
        assertEquals(1L, settings.getProcessingIntervalTicks());
    }

    @Test
    void testLegacyThrottleMaxChunksClamped() {
        LegacyThrottleSettings settings = new LegacyThrottleSettings(true, 3L, 0);
        assertEquals(1, settings.getMaxChunksPerTick());
    }
}
