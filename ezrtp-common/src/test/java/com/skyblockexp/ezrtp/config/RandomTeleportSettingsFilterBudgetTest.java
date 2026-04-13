package com.skyblockexp.ezrtp.config;

import com.skyblockexp.ezrtp.config.biome.BiomeSearchSettings;
import com.skyblockexp.ezrtp.teleport.LocationValidator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class RandomTeleportSettingsFilterBudgetTest {

    private static final Logger LOG = Logger.getLogger("RandomTeleportSettingsFilterBudgetTest");

    // -----------------------------------------------------------------------
    // Conditional filter-aware defaults
    // -----------------------------------------------------------------------

    @Test
    void withBiomeIncludeAndNoBudget_autoApplies100msAnd10Chunks() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.include", Arrays.asList("PLAINS"));

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        BiomeSearchSettings budget = s.getBiomeSearchSettings();
        assertEquals(100, budget.getMaxWallClockMillis(),
            "100ms wall-clock should be auto-applied when include filter is active");
        assertEquals(10, budget.getMaxChunkLoads(),
            "10 chunk-load cap should be auto-applied when include filter is active");
    }

    @Test
    void withBiomeExcludeAndNoBudget_autoApplies100msAnd10Chunks() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.exclude", Arrays.asList("OCEAN"));

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        BiomeSearchSettings budget = s.getBiomeSearchSettings();
        assertEquals(100, budget.getMaxWallClockMillis());
        assertEquals(10, budget.getMaxChunkLoads());
    }

    @Test
    void emptyFilters_keepsUnlimitedDefaults() {
        YamlConfiguration cfg = new YamlConfiguration();

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        BiomeSearchSettings budget = s.getBiomeSearchSettings();
        assertEquals(0, budget.getMaxWallClockMillis(),
            "No biome filters should keep unlimited wall-clock (0)");
        assertEquals(0, budget.getMaxChunkLoads(),
            "No biome filters should keep unlimited chunk-load cap (0)");
    }

    @Test
    void withBiomeIncludeAndExplicitBudget_explicitOverridesAutoDefault() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.include", Arrays.asList("PLAINS"));
        cfg.set("biomes.biome-filtering.performance-budget.max-total-time-ms", 500);
        cfg.set("biomes.biome-filtering.performance-budget.max-chunk-loads", 20);

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        BiomeSearchSettings budget = s.getBiomeSearchSettings();
        assertEquals(500, budget.getMaxWallClockMillis(),
            "Explicit max-total-time-ms should override auto-default");
        assertEquals(20, budget.getMaxChunkLoads(),
            "Explicit max-chunk-loads should override auto-default");
    }

    // -----------------------------------------------------------------------
    // biome-filtering.enabled master toggle
    // -----------------------------------------------------------------------

    @Test
    void biomeFilteringEnabled_defaultsToTrue() {
        YamlConfiguration cfg = new YamlConfiguration();

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        assertTrue(s.isBiomeFilteringEnabled());
    }

    @Test
    void biomeFilteringEnabled_false_parsedCorrectly() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.biome-filtering.enabled", false);

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        assertFalse(s.isBiomeFilteringEnabled());
    }

    @Test
    void biomeFilteringEnabled_false_hasBiomeFiltersReturnsFalse() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.include", Arrays.asList("PLAINS"));
        cfg.set("biomes.biome-filtering.enabled", false);

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        assertFalse(LocationValidator.hasBiomeFilters(s),
            "hasBiomeFilters must respect the master enabled toggle");
    }

    @Test
    void biomeFilteringEnabled_true_withIncludes_hasBiomeFiltersReturnsTrue() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.include", Arrays.asList("PLAINS"));
        cfg.set("biomes.biome-filtering.enabled", true);

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        assertTrue(LocationValidator.hasBiomeFilters(s));
    }

    // -----------------------------------------------------------------------
    // New biome-filtering.performance-budget config path
    // -----------------------------------------------------------------------

    @Test
    void performanceBudget_maxTotalTimeMs_parsedCorrectly() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.include", Arrays.asList("PLAINS"));
        cfg.set("biomes.biome-filtering.performance-budget.max-total-time-ms", 250);

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        assertEquals(250, s.getBiomeSearchSettings().getMaxWallClockMillis());
    }

    @Test
    void performanceBudget_fallbackOnTimeout_parsedCorrectly() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.biome-filtering.performance-budget.fallback-on-timeout", "cached-location");

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        assertEquals(BiomeSearchSettings.FailoverMode.CACHE,
            s.getBiomeSearchSettings().getFailoverMode());
    }

    @Test
    void performanceBudget_asyncModeFalse_parsedCorrectly() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.biome-filtering.performance-budget.async-mode", false);

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        assertFalse(s.getBiomeSearchSettings().isAsyncMode());
    }

    // -----------------------------------------------------------------------
    // Legacy biomes.search path still works
    // -----------------------------------------------------------------------

    @Test
    void legacySearchSection_stillParsedCorrectly() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.search.max-wait-seconds", 3);
        cfg.set("biomes.search.max-chunk-loads", 8);
        cfg.set("biomes.search.failover-mode", "ABORT");

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        BiomeSearchSettings budget = s.getBiomeSearchSettings();
        assertEquals(3000, budget.getMaxWallClockMillis());
        assertEquals(8, budget.getMaxChunkLoads());
        assertEquals(BiomeSearchSettings.FailoverMode.ABORT, budget.getFailoverMode());
    }

    @Test
    void newPathTakesPrecedenceOverLegacySearchSection() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.search.max-wait-seconds", 99);
        cfg.set("biomes.biome-filtering.performance-budget.max-total-time-ms", 123);

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        // New path wins
        assertEquals(123, s.getBiomeSearchSettings().getMaxWallClockMillis());
    }

    // -----------------------------------------------------------------------
    // Fallback inheritance in 3-arg fromConfiguration
    // -----------------------------------------------------------------------

    @Test
    void fallbackMerge_biomeFilteringEnabled_inheritedWhenNotOverridden() {
        YamlConfiguration baseCfg = new YamlConfiguration();
        baseCfg.set("biomes.biome-filtering.enabled", false);
        RandomTeleportSettings base = RandomTeleportSettings.fromConfiguration(baseCfg, LOG);

        YamlConfiguration override = new YamlConfiguration();
        RandomTeleportSettings merged = RandomTeleportSettings.fromConfiguration(override, LOG, base);

        assertFalse(merged.isBiomeFilteringEnabled(),
            "biomeFilteringEnabled should be inherited from fallback when not explicitly set");
    }

    @Test
    void fallbackMerge_biomeFilteringEnabled_overriddenWhenExplicitlySet() {
        YamlConfiguration baseCfg = new YamlConfiguration();
        baseCfg.set("biomes.biome-filtering.enabled", false);
        RandomTeleportSettings base = RandomTeleportSettings.fromConfiguration(baseCfg, LOG);

        YamlConfiguration override = new YamlConfiguration();
        override.set("biomes.biome-filtering.enabled", true);
        RandomTeleportSettings merged = RandomTeleportSettings.fromConfiguration(override, LOG, base);

        assertTrue(merged.isBiomeFilteringEnabled(),
            "biomeFilteringEnabled should be overridden when explicitly set in override config");
    }

    // -----------------------------------------------------------------------
    // biomes.enabled master switch
    // -----------------------------------------------------------------------

    @Test
    void biomeSystemEnabled_defaultsToTrue() {
        YamlConfiguration cfg = new YamlConfiguration();

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        assertTrue(s.isBiomeSystemEnabled());
    }

    @Test
    void biomeSystemEnabled_false_parsedCorrectly() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.enabled", false);

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        assertFalse(s.isBiomeSystemEnabled());
    }

    @Test
    void biomeSystemEnabled_false_withIncludes_hasBiomeFiltersReturnsFalse() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("biomes.enabled", false);
        cfg.set("biomes.include", Arrays.asList("PLAINS"));

        RandomTeleportSettings s = RandomTeleportSettings.fromConfiguration(cfg, LOG);

        assertFalse(LocationValidator.hasBiomeFilters(s),
            "hasBiomeFilters must return false when biomes.enabled is false, even with non-empty include list");
    }

    @Test
    void fallbackMerge_biomeSystemEnabled_inheritedWhenNotOverridden() {
        YamlConfiguration baseCfg = new YamlConfiguration();
        baseCfg.set("biomes.enabled", false);
        RandomTeleportSettings base = RandomTeleportSettings.fromConfiguration(baseCfg, LOG);

        YamlConfiguration override = new YamlConfiguration();
        RandomTeleportSettings merged = RandomTeleportSettings.fromConfiguration(override, LOG, base);

        assertFalse(merged.isBiomeSystemEnabled(),
            "biomeSystemEnabled should be inherited from fallback when not explicitly set");
    }

    @Test
    void fallbackMerge_biomeSystemEnabled_overriddenWhenExplicitlySet() {
        YamlConfiguration baseCfg = new YamlConfiguration();
        baseCfg.set("biomes.enabled", false);
        RandomTeleportSettings base = RandomTeleportSettings.fromConfiguration(baseCfg, LOG);

        YamlConfiguration override = new YamlConfiguration();
        override.set("biomes.enabled", true);
        RandomTeleportSettings merged = RandomTeleportSettings.fromConfiguration(override, LOG, base);

        assertTrue(merged.isBiomeSystemEnabled(),
            "biomeSystemEnabled should be overridden when explicitly set in override config");
    }
}
