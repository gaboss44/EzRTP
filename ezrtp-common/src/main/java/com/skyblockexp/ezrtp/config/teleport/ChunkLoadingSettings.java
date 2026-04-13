package com.skyblockexp.ezrtp.config.teleport;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Global chunk loading configuration. On Paper 1.21+, the async chunk API removes the need
 * for tick-based throttling; use {@code use-paper-async-api: auto-detect} (the default) to
 * bypass the throttle queue automatically on qualifying servers.
 *
 * <p>The legacy flat format ({@code enabled/interval-ticks/max-chunks-per-tick} at the top
 * level) is still accepted and silently migrated: it maps to {@link PaperAsyncApiMode#AUTO_DETECT}
 * with the legacy throttle settings preserved.
 */
public final class ChunkLoadingSettings {

    /** Controls whether the Paper async chunk API is used to bypass the throttle queue. */
    public enum PaperAsyncApiMode {
        /** Use the async API when Paper 1.21+ is detected at runtime (default). */
        AUTO_DETECT,
        /** Always use the async passthrough, regardless of platform. */
        ALWAYS,
        /** Never use the async passthrough; always apply the legacy throttle. */
        NEVER;

        /** Parses {@code auto-detect}, {@code true}/{@code always}, {@code false}/{@code never}. */
        public static PaperAsyncApiMode fromString(String value, PaperAsyncApiMode defaultMode) {
            if (value == null) return defaultMode;
            return switch (value.toLowerCase(java.util.Locale.ROOT).trim()) {
                case "auto-detect", "auto" -> AUTO_DETECT;
                case "always", "true" -> ALWAYS;
                case "never", "false" -> NEVER;
                default -> defaultMode;
            };
        }
    }

    /** Throttle settings used on platforms that do not support the Paper async API. */
    public static final class LegacyThrottleSettings {

        private final boolean enabled;
        private final long processingIntervalTicks;
        private final int maxChunksPerTick;

        public LegacyThrottleSettings(boolean enabled, long processingIntervalTicks, int maxChunksPerTick) {
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

        public static LegacyThrottleSettings defaults() {
            return new LegacyThrottleSettings(true, 3L, 2);
        }

        static LegacyThrottleSettings fromSection(ConfigurationSection section, LegacyThrottleSettings fallback) {
            LegacyThrottleSettings def = fallback != null ? fallback : defaults();
            if (section == null) return def;
            boolean enabled = section.getBoolean("enabled", def.isEnabled());
            long interval = section.getLong("interval-ticks", def.getProcessingIntervalTicks());
            int maxChunks = section.getInt("max-chunks-per-tick", def.getMaxChunksPerTick());
            return new LegacyThrottleSettings(enabled, interval, maxChunks);
        }
    }

    private final PaperAsyncApiMode usePaperAsyncApi;
    private final LegacyThrottleSettings legacyThrottle;

    public ChunkLoadingSettings(PaperAsyncApiMode usePaperAsyncApi, LegacyThrottleSettings legacyThrottle) {
        this.usePaperAsyncApi = usePaperAsyncApi != null ? usePaperAsyncApi : PaperAsyncApiMode.AUTO_DETECT;
        this.legacyThrottle = legacyThrottle != null ? legacyThrottle : LegacyThrottleSettings.defaults();
    }

    // ── Legacy convenience constructor (used by RareBiomeOptimizationSettings and similar) ──

    /**
     * Creates a settings object with {@link PaperAsyncApiMode#AUTO_DETECT} and the supplied
     * legacy throttle values. Maintained for callers that still use the old three-arg form.
     */
    public ChunkLoadingSettings(boolean enabled, long processingIntervalTicks, int maxChunksPerTick) {
        this(PaperAsyncApiMode.AUTO_DETECT, new LegacyThrottleSettings(enabled, processingIntervalTicks, maxChunksPerTick));
    }

    public PaperAsyncApiMode getUsePaperAsyncApi() {
        return usePaperAsyncApi;
    }

    public LegacyThrottleSettings getLegacyThrottle() {
        return legacyThrottle;
    }

    // ── Legacy accessors kept for backward-compatible call sites ──

    /** @deprecated Use {@link #getLegacyThrottle()}{@code .isEnabled()} */
    @Deprecated
    public boolean isEnabled() {
        return legacyThrottle.isEnabled();
    }

    /** @deprecated Use {@link #getLegacyThrottle()}{@code .getProcessingIntervalTicks()} */
    @Deprecated
    public long getProcessingIntervalTicks() {
        return legacyThrottle.getProcessingIntervalTicks();
    }

    /** @deprecated Use {@link #getLegacyThrottle()}{@code .getMaxChunksPerTick()} */
    @Deprecated
    public int getMaxChunksPerTick() {
        return legacyThrottle.getMaxChunksPerTick();
    }

    public static ChunkLoadingSettings defaults() {
        return new ChunkLoadingSettings(PaperAsyncApiMode.AUTO_DETECT, LegacyThrottleSettings.defaults());
    }

    public static ChunkLoadingSettings fromConfiguration(ConfigurationSection section, ChunkLoadingSettings fallback) {
        ChunkLoadingSettings def = fallback != null ? fallback : defaults();
        if (section == null) {
            return def;
        }

        // New format: has use-paper-async-api or legacy-throttle sub-section.
        // Old (legacy) flat format: has enabled/interval-ticks/max-chunks-per-tick at the top level.
        boolean hasNewKey = section.contains("use-paper-async-api") || section.isConfigurationSection("legacy-throttle");
        if (hasNewKey) {
            String modeStr = section.getString("use-paper-async-api", null);
            PaperAsyncApiMode mode = PaperAsyncApiMode.fromString(modeStr, def.getUsePaperAsyncApi());
            ConfigurationSection legacySection = section.getConfigurationSection("legacy-throttle");
            LegacyThrottleSettings legacy = LegacyThrottleSettings.fromSection(legacySection, def.getLegacyThrottle());
            return new ChunkLoadingSettings(mode, legacy);
        }

        // Legacy flat format — silently migrate.
        boolean enabled = section.getBoolean("enabled", def.getLegacyThrottle().isEnabled());
        long interval = section.getLong("interval-ticks", def.getLegacyThrottle().getProcessingIntervalTicks());
        int maxChunks = section.getInt("max-chunks-per-tick", def.getLegacyThrottle().getMaxChunksPerTick());
        return new ChunkLoadingSettings(
                PaperAsyncApiMode.AUTO_DETECT,
                new LegacyThrottleSettings(enabled, interval, maxChunks));
    }
}
