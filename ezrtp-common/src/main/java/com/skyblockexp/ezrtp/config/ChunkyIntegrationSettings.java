package com.skyblockexp.ezrtp.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Chunky integration configuration for pregenerating RTP search areas.
 */
public final class ChunkyIntegrationSettings {

    private final boolean enabled;
    private final boolean autoPregenerate;
    private final String shape;
    private final String pattern;
    private final boolean memorySafetyEnabled;
    private final long minFreeMemoryMb;
    private final int maxCoordinatorEntries;
    private final long lowMemoryRetentionMinutes;

    public ChunkyIntegrationSettings(boolean enabled, boolean autoPregenerate, String shape, String pattern,
                                   boolean memorySafetyEnabled, long minFreeMemoryMb, int maxCoordinatorEntries,
                                   long lowMemoryRetentionMinutes) {
        this.enabled = enabled;
        this.autoPregenerate = autoPregenerate;
        this.shape = shape != null ? shape : "circle";
        this.pattern = pattern != null ? pattern : "concentric";
        this.memorySafetyEnabled = memorySafetyEnabled;
        this.minFreeMemoryMb = minFreeMemoryMb;
        this.maxCoordinatorEntries = maxCoordinatorEntries;
        this.lowMemoryRetentionMinutes = lowMemoryRetentionMinutes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAutoPregenerate() {
        return autoPregenerate;
    }

    public String getShape() {
        return shape;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean isMemorySafetyEnabled() {
        return memorySafetyEnabled;
    }

    public long getMinFreeMemoryMb() {
        return minFreeMemoryMb;
    }

    public int getMaxCoordinatorEntries() {
        return maxCoordinatorEntries;
    }

    public long getLowMemoryRetentionMinutes() {
        return lowMemoryRetentionMinutes;
    }

    public static ChunkyIntegrationSettings defaults() {
        return new ChunkyIntegrationSettings(false, false, "circle", "concentric", true, 512L, 10000, 15L);
    }

    public static ChunkyIntegrationSettings fromConfiguration(ConfigurationSection section, ChunkyIntegrationSettings fallback) {
        ChunkyIntegrationSettings defaults = fallback != null ? fallback : defaults();
        if (section == null) {
            return defaults;
        }
        boolean enabled = section.getBoolean("enabled", defaults.isEnabled());
        boolean autoPregenerate = section.getBoolean("auto-pregenerate", defaults.isAutoPregenerate());
        String shape = section.getString("shape", defaults.getShape());
        String pattern = section.getString("pattern", defaults.getPattern());

        // Memory safety settings
        ConfigurationSection memorySection = section.getConfigurationSection("memory-safety");
        boolean memorySafetyEnabled = memorySection != null ?
            memorySection.getBoolean("enabled", defaults.isMemorySafetyEnabled()) : defaults.isMemorySafetyEnabled();
        long minFreeMemoryMb = memorySection != null ?
            memorySection.getLong("min-free-memory-mb", defaults.getMinFreeMemoryMb()) : defaults.getMinFreeMemoryMb();
        int maxCoordinatorEntries = memorySection != null ?
            memorySection.getInt("max-coordinator-entries", defaults.getMaxCoordinatorEntries()) : defaults.getMaxCoordinatorEntries();
        long lowMemoryRetentionMinutes = memorySection != null ?
            memorySection.getLong("low-memory-retention-minutes", defaults.getLowMemoryRetentionMinutes()) : defaults.getLowMemoryRetentionMinutes();

        return new ChunkyIntegrationSettings(enabled, autoPregenerate, shape, pattern,
                                           memorySafetyEnabled, minFreeMemoryMb, maxCoordinatorEntries,
                                           lowMemoryRetentionMinutes);
    }
}