package com.skyblockexp.ezrtp.config.teleport;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Global chunk loading throttle configuration. Controls how aggressively the
 * chunk load queue processes work, independent of biome optimizations.
 */
public final class ChunkLoadingSettings {

    private final boolean enabled;
    private final long processingIntervalTicks;
    private final int maxChunksPerTick;

    public ChunkLoadingSettings(boolean enabled, long processingIntervalTicks, int maxChunksPerTick) {
        this.enabled = enabled;
        this.processingIntervalTicks = Math.max(1L, processingIntervalTicks);
        this.maxChunksPerTick = Math.max(1, maxChunksPerTick);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public long getProcessingIntervalTicks() {
        return processingIntervalTicks;
    }

    public int getMaxChunksPerTick() {
        return maxChunksPerTick;
    }

    public static ChunkLoadingSettings defaults() {
        return new ChunkLoadingSettings(true, 3L, 2);
    }

    public static ChunkLoadingSettings fromConfiguration(ConfigurationSection section, ChunkLoadingSettings fallback) {
        ChunkLoadingSettings defaults = fallback != null ? fallback : defaults();
        if (section == null) {
            return defaults;
        }
        boolean enabled = section.getBoolean("enabled", defaults.isEnabled());
        long interval = section.getLong("interval-ticks", defaults.getProcessingIntervalTicks());
        int maxChunks = section.getInt("max-chunks-per-tick", defaults.getMaxChunksPerTick());
        return new ChunkLoadingSettings(enabled, interval, maxChunks);
    }
}
