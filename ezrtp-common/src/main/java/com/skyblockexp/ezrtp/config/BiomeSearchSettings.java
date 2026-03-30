package com.skyblockexp.ezrtp.config;

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
            try {
                return FailoverMode.valueOf(value.trim().toUpperCase());
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

    public BiomeSearchSettings(int maxWallClockMillis,
                               int maxWallClockMillisRare,
                               int maxBiomeRejections,
                               int maxBiomeRejectionsRare,
                               int maxChunkLoads,
                               int maxChunkLoadsRare,
                               int minBiomeAttempts,
                               FailoverMode failoverMode) {
        this.maxWallClockMillis = Math.max(0, maxWallClockMillis);
        this.maxWallClockMillisRare = Math.max(0, maxWallClockMillisRare);
        this.maxBiomeRejections = Math.max(0, maxBiomeRejections);
        this.maxBiomeRejectionsRare = Math.max(0, maxBiomeRejectionsRare);
        this.maxChunkLoads = Math.max(0, maxChunkLoads);
        this.maxChunkLoadsRare = Math.max(0, maxChunkLoadsRare);
        this.minBiomeAttempts = Math.max(1, minBiomeAttempts);
        this.failoverMode = failoverMode != null ? failoverMode : FailoverMode.CACHE;
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

    public static BiomeSearchSettings defaults() {
        return new BiomeSearchSettings(0, 5000, 0, 64, 0, 16, 96, FailoverMode.CACHE);
    }

    public static BiomeSearchSettings fromConfiguration(ConfigurationSection section, BiomeSearchSettings fallback) {
        if (section == null) {
            return fallback != null ? fallback : defaults();
        }
        BiomeSearchSettings defaults = fallback != null ? fallback : defaults();
        int maxWallClock = resolveWallClock(section, "max-wait-seconds", "max-wall-clock-millis", defaults.getMaxWallClockMillis());
        int maxWallClockRare = resolveWallClock(section, "max-wait-seconds-rare", "max-wall-clock-millis-rare", defaults.getMaxWallClockMillisRare());
        int maxBiomeRejections = Math.max(0, section.getInt("max-biome-rejections", defaults.getMaxBiomeRejections()));
        int maxBiomeRejectionsRare = Math.max(0, section.getInt("max-biome-rejections-rare", defaults.getMaxBiomeRejectionsRare()));
        int maxChunkLoads = Math.max(0, section.getInt("max-chunk-loads", defaults.getMaxChunkLoads()));
        int maxChunkLoadsRare = Math.max(0, section.getInt("max-chunk-loads-rare", defaults.getMaxChunkLoadsRare()));
        int minBiomeAttempts = Math.max(1, section.getInt("min-biome-attempts", defaults.getMinBiomeAttempts()));
        FailoverMode failoverMode = FailoverMode.fromString(section.getString("failover-mode"), defaults.getFailoverMode());
        return new BiomeSearchSettings(maxWallClock, maxWallClockRare, maxBiomeRejections, maxBiomeRejectionsRare,
            maxChunkLoads, maxChunkLoadsRare, minBiomeAttempts, failoverMode);
    }

    private static int resolveWallClock(ConfigurationSection section,
                                        String secondsKey,
                                        String millisKey,
                                        int fallbackMillis) {
        if (section.isSet(secondsKey)) {
            int seconds = Math.max(0, section.getInt(secondsKey));
            return seconds * 1000;
        }
        if (section.isSet(millisKey)) {
            return Math.max(0, section.getInt(millisKey));
        }
        return Math.max(0, fallbackMillis);
    }
}