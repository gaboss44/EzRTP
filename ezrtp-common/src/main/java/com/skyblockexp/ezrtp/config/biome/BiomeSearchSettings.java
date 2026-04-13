package com.skyblockexp.ezrtp.config.biome;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Configuration container for biome-constrained search safeguards.
 */
public final class BiomeSearchSettings {

    public enum FailoverMode {
        CACHE,
        GENERIC,
        ABORT;

        public static FailoverMode fromString(String value, FailoverMode fallback) {
            if (value == null) {
                return fallback;
            }
            String normalized = value.trim().toUpperCase().replace('-', '_');
            // Accept "cached-location" / "cached_location" as an alias for CACHE
            if ("CACHED_LOCATION".equals(normalized)) {
                return CACHE;
            }
            try {
                return FailoverMode.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                return fallback;
            }
        }
    }

    private final int maxWallClockMillis;
    private final int maxWallClockMillisRare;
    private final int maxBiomeRejections;
    private final int maxBiomeRejectionsRare;
    private final int maxChunkLoads;
    private final int maxChunkLoadsRare;
    private final int minBiomeAttempts;
    private final FailoverMode failoverMode;
    private final boolean asyncMode;

    public BiomeSearchSettings(int maxWallClockMillis,
                               int maxWallClockMillisRare,
                               int maxBiomeRejections,
                               int maxBiomeRejectionsRare,
                               int maxChunkLoads,
                               int maxChunkLoadsRare,
                               int minBiomeAttempts,
                               FailoverMode failoverMode) {
        this(maxWallClockMillis, maxWallClockMillisRare, maxBiomeRejections, maxBiomeRejectionsRare,
            maxChunkLoads, maxChunkLoadsRare, minBiomeAttempts, failoverMode, true);
    }

    public BiomeSearchSettings(int maxWallClockMillis,
                               int maxWallClockMillisRare,
                               int maxBiomeRejections,
                               int maxBiomeRejectionsRare,
                               int maxChunkLoads,
                               int maxChunkLoadsRare,
                               int minBiomeAttempts,
                               FailoverMode failoverMode,
                               boolean asyncMode) {
        this.maxWallClockMillis = Math.max(0, maxWallClockMillis);
        this.maxWallClockMillisRare = Math.max(0, maxWallClockMillisRare);
        this.maxBiomeRejections = Math.max(0, maxBiomeRejections);
        this.maxBiomeRejectionsRare = Math.max(0, maxBiomeRejectionsRare);
        this.maxChunkLoads = Math.max(0, maxChunkLoads);
        this.maxChunkLoadsRare = Math.max(0, maxChunkLoadsRare);
        this.minBiomeAttempts = Math.max(1, minBiomeAttempts);
        this.failoverMode = failoverMode != null ? failoverMode : FailoverMode.CACHE;
        this.asyncMode = asyncMode;
    }

    public int getMaxWallClockMillis() {
        return maxWallClockMillis;
    }

    public int getMaxWallClockMillisRare() {
        return maxWallClockMillisRare;
    }

    public int getMaxBiomeRejections() {
        return maxBiomeRejections;
    }

    public int getMaxBiomeRejectionsRare() {
        return maxBiomeRejectionsRare;
    }

    public int getMaxChunkLoads() {
        return maxChunkLoads;
    }

    public int getMaxChunkLoadsRare() {
        return maxChunkLoadsRare;
    }

    public int getMinBiomeAttempts() {
        return minBiomeAttempts;
    }

    public FailoverMode getFailoverMode() {
        return failoverMode;
    }

    /**
     * When {@code false}, the biome search is serialised through a single dedicated thread
     * (bounded concurrency) instead of the common fork-join pool. Defaults to {@code true}.
     */
    public boolean isAsyncMode() {
        return asyncMode;
    }

    public static BiomeSearchSettings defaults() {
        return new BiomeSearchSettings(0, 5000, 0, 64, 0, 16, 96, FailoverMode.CACHE, true);
    }

    /**
     * Returns performance-oriented defaults applied automatically when include/exclude biome
     * filters are active but no explicit budget has been configured by the server operator.
     * Tighter wall-clock (100 ms) and chunk-load (10) caps prevent runaway searches.
     */
    public static BiomeSearchSettings filterAwareDefaults() {
        return new BiomeSearchSettings(100, 8000, 0, 64, 10, 16, 96, FailoverMode.CACHE, true);
    }

    public static BiomeSearchSettings fromConfiguration(ConfigurationSection section, BiomeSearchSettings fallback) {
        if (section == null) {
            return fallback != null ? fallback : defaults();
        }
        BiomeSearchSettings base = fallback != null ? fallback : defaults();
        int maxWallClock = resolveWallClock(section, "max-wait-seconds", "max-total-time-ms", "max-wall-clock-millis", base.getMaxWallClockMillis());
        int maxWallClockRare = resolveWallClock(section, "max-wait-seconds-rare", "max-total-time-ms-rare", "max-wall-clock-millis-rare", base.getMaxWallClockMillisRare());
        int maxBiomeRejections = Math.max(0, section.getInt("max-biome-rejections", base.getMaxBiomeRejections()));
        int maxBiomeRejectionsRare = Math.max(0, section.getInt("max-biome-rejections-rare", base.getMaxBiomeRejectionsRare()));
        int maxChunkLoads = Math.max(0, section.getInt("max-chunk-loads", base.getMaxChunkLoads()));
        int maxChunkLoadsRare = Math.max(0, section.getInt("max-chunk-loads-rare", base.getMaxChunkLoadsRare()));
        int minBiomeAttempts = Math.max(1, section.getInt("min-biome-attempts", base.getMinBiomeAttempts()));
        // "fallback-on-timeout" takes precedence over the legacy "failover-mode" key
        String failoverRaw = section.isSet("fallback-on-timeout")
            ? section.getString("fallback-on-timeout")
            : section.getString("failover-mode");
        FailoverMode failoverMode = FailoverMode.fromString(failoverRaw, base.getFailoverMode());
        boolean asyncMode = section.getBoolean("async-mode", base.isAsyncMode());
        return new BiomeSearchSettings(maxWallClock, maxWallClockRare, maxBiomeRejections, maxBiomeRejectionsRare,
            maxChunkLoads, maxChunkLoadsRare, minBiomeAttempts, failoverMode, asyncMode);
    }

    private static int resolveWallClock(ConfigurationSection section,
                                        String secondsKey,
                                        String millisKey,
                                        String legacyMillisKey,
                                        int fallbackMillis) {
        // Priority: seconds key (legacy) > new millis key > legacy millis alias > fallback
        if (section.isSet(secondsKey)) {
            int seconds = Math.max(0, section.getInt(secondsKey));
            return seconds * 1000;
        }
        if (section.isSet(millisKey)) {
            return Math.max(0, section.getInt(millisKey));
        }
        if (section.isSet(legacyMillisKey)) {
            return Math.max(0, section.getInt(legacyMillisKey));
        }
        return Math.max(0, fallbackMillis);
    }
}