package com.skyblockexp.ezrtp.config.biome;

import com.skyblockexp.ezrtp.config.biome.BiomeSearchSettings;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BiomeSearchSettingsTest {

    // -----------------------------------------------------------------------
    // Static factories
    // -----------------------------------------------------------------------

    @Test
    void defaults_hasUnlimitedBudget() {
        BiomeSearchSettings s = BiomeSearchSettings.defaults();
        assertEquals(0, s.getMaxWallClockMillis());
        assertEquals(0, s.getMaxChunkLoads());
        assertTrue(s.isAsyncMode());
        assertEquals(BiomeSearchSettings.FailoverMode.CACHE, s.getFailoverMode());
    }

    @Test
    void filterAwareDefaults_hasTighterBudget() {
        BiomeSearchSettings s = BiomeSearchSettings.filterAwareDefaults();
        assertEquals(100, s.getMaxWallClockMillis());
        assertEquals(10, s.getMaxChunkLoads());
        assertEquals(8000, s.getMaxWallClockMillisRare());
        assertEquals(16, s.getMaxChunkLoadsRare());
        assertTrue(s.isAsyncMode());
        assertEquals(BiomeSearchSettings.FailoverMode.CACHE, s.getFailoverMode());
    }

    // -----------------------------------------------------------------------
    // FailoverMode aliases
    // -----------------------------------------------------------------------

    @Test
    void failoverMode_cachedLocation_mapsToCACHE() {
        assertEquals(BiomeSearchSettings.FailoverMode.CACHE,
            BiomeSearchSettings.FailoverMode.fromString("cached-location", BiomeSearchSettings.FailoverMode.ABORT));
    }

    @Test
    void failoverMode_cachedLocationUnderscore_mapsToCACHE() {
        assertEquals(BiomeSearchSettings.FailoverMode.CACHE,
            BiomeSearchSettings.FailoverMode.fromString("cached_location", BiomeSearchSettings.FailoverMode.ABORT));
    }

    @Test
    void failoverMode_CACHE_stillWorks() {
        assertEquals(BiomeSearchSettings.FailoverMode.CACHE,
            BiomeSearchSettings.FailoverMode.fromString("CACHE", BiomeSearchSettings.FailoverMode.ABORT));
    }

    @Test
    void failoverMode_abort_mapsToABORT() {
        assertEquals(BiomeSearchSettings.FailoverMode.ABORT,
            BiomeSearchSettings.FailoverMode.fromString("abort", BiomeSearchSettings.FailoverMode.CACHE));
    }

    @Test
    void failoverMode_unknown_returnsFallback() {
        assertEquals(BiomeSearchSettings.FailoverMode.GENERIC,
            BiomeSearchSettings.FailoverMode.fromString("nonsense", BiomeSearchSettings.FailoverMode.GENERIC));
    }

    @Test
    void failoverMode_null_returnsFallback() {
        assertEquals(BiomeSearchSettings.FailoverMode.CACHE,
            BiomeSearchSettings.FailoverMode.fromString(null, BiomeSearchSettings.FailoverMode.CACHE));
    }

    // -----------------------------------------------------------------------
    // fromConfiguration — new keys
    // -----------------------------------------------------------------------

    @Test
    void fromConfiguration_maxTotalTimeMs_parsedAsMillis() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("max-total-time-ms", 250);

        BiomeSearchSettings s = BiomeSearchSettings.fromConfiguration(cfg, BiomeSearchSettings.defaults());
        assertEquals(250, s.getMaxWallClockMillis());
    }

    @Test
    void fromConfiguration_maxTotalTimeMsRare_parsedAsMillis() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("max-total-time-ms-rare", 4000);

        BiomeSearchSettings s = BiomeSearchSettings.fromConfiguration(cfg, BiomeSearchSettings.defaults());
        assertEquals(4000, s.getMaxWallClockMillisRare());
    }

    @Test
    void fromConfiguration_fallbackOnTimeout_takesPrecedenceOverFailoverMode() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("failover-mode", "ABORT");
        cfg.set("fallback-on-timeout", "cached-location");

        BiomeSearchSettings s = BiomeSearchSettings.fromConfiguration(cfg, BiomeSearchSettings.defaults());
        assertEquals(BiomeSearchSettings.FailoverMode.CACHE, s.getFailoverMode());
    }

    @Test
    void fromConfiguration_asyncModeFalse_parsedCorrectly() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("async-mode", false);

        BiomeSearchSettings s = BiomeSearchSettings.fromConfiguration(cfg, BiomeSearchSettings.defaults());
        assertFalse(s.isAsyncMode());
    }

    @Test
    void fromConfiguration_asyncModeTrue_parsedCorrectly() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("async-mode", true);

        BiomeSearchSettings s = BiomeSearchSettings.fromConfiguration(cfg, BiomeSearchSettings.defaults());
        assertTrue(s.isAsyncMode());
    }

    // -----------------------------------------------------------------------
    // Backward-compatibility — legacy keys
    // -----------------------------------------------------------------------

    @Test
    void fromConfiguration_legacyMaxWaitSeconds_stillWorks() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("max-wait-seconds", 3); // 3 s = 3000 ms

        BiomeSearchSettings s = BiomeSearchSettings.fromConfiguration(cfg, BiomeSearchSettings.defaults());
        assertEquals(3000, s.getMaxWallClockMillis());
    }

    @Test
    void fromConfiguration_legacyMaxWaitSecondsRare_stillWorks() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("max-wait-seconds-rare", 5);

        BiomeSearchSettings s = BiomeSearchSettings.fromConfiguration(cfg, BiomeSearchSettings.defaults());
        assertEquals(5000, s.getMaxWallClockMillisRare());
    }

    @Test
    void fromConfiguration_legacyFailoverMode_stillWorks() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("failover-mode", "ABORT");

        BiomeSearchSettings s = BiomeSearchSettings.fromConfiguration(cfg, BiomeSearchSettings.defaults());
        assertEquals(BiomeSearchSettings.FailoverMode.ABORT, s.getFailoverMode());
    }

    @Test
    void fromConfiguration_maxWaitSeconds_takesPrecedenceOverMaxTotalTimeMs() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("max-wait-seconds", 2);       // 2 s = 2000 ms; wins per priority
        cfg.set("max-total-time-ms", 9999);   // should be ignored

        BiomeSearchSettings s = BiomeSearchSettings.fromConfiguration(cfg, BiomeSearchSettings.defaults());
        assertEquals(2000, s.getMaxWallClockMillis());
    }

    // -----------------------------------------------------------------------
    // 8-arg constructor backward compat
    // -----------------------------------------------------------------------

    @Test
    void eightArgConstructor_defaultsAsyncModeToTrue() {
        BiomeSearchSettings s = new BiomeSearchSettings(100, 5000, 0, 64, 10, 16, 96,
            BiomeSearchSettings.FailoverMode.CACHE);
        assertTrue(s.isAsyncMode());
    }

    @Test
    void nineArgConstructor_asyncModeFalse() {
        BiomeSearchSettings s = new BiomeSearchSettings(100, 5000, 0, 64, 10, 16, 96,
            BiomeSearchSettings.FailoverMode.CACHE, false);
        assertFalse(s.isAsyncMode());
    }

    // -----------------------------------------------------------------------
    // Null handling
    // -----------------------------------------------------------------------

    @Test
    void fromConfiguration_nullSection_returnsFallback() {
        BiomeSearchSettings fallback = BiomeSearchSettings.filterAwareDefaults();
        BiomeSearchSettings s = BiomeSearchSettings.fromConfiguration(null, fallback);
        assertEquals(fallback, s);
    }

    @Test
    void fromConfiguration_nullSectionAndNullFallback_returnsDefaults() {
        BiomeSearchSettings s = BiomeSearchSettings.fromConfiguration(null, null);
        assertEquals(0, s.getMaxWallClockMillis());
    }
}
