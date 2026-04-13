package com.skyblockexp.ezrtp.config;
import com.skyblockexp.ezrtp.config.biome.BiomePreCacheSettings;
import com.skyblockexp.ezrtp.config.biome.BiomeSearchSettings;
import com.skyblockexp.ezrtp.config.biome.ChunkyIntegrationSettings;
import com.skyblockexp.ezrtp.config.safety.SafetySettings;
import com.skyblockexp.ezrtp.config.teleport.RtpLimitSettings;

/**
 * Central repository of default numeric constants used across the settings classes.
 *
 * <p>Gathering these values here makes it easy to see, compare, and adjust tuning parameters
 * without hunting through individual setting classes. Each constant is documented with its
 * meaning, unit, and which setting class consumes it.
 *
 * <p>This class is not instantiable — all members are {@code public static final} constants.
 */
public final class ConfigurationDefaults {

    private ConfigurationDefaults() {}

    // -------------------------------------------------------------------------
    // RTP limits (EzRtpConfiguration / RtpLimitSettings)
    // -------------------------------------------------------------------------

    /** Default cooldown between random teleports, in seconds. */
    public static final int DEFAULT_COOLDOWN_SECONDS = 300;

    /** Default maximum number of random teleports per day. */
    public static final int DEFAULT_DAILY_LIMIT = 10;

    /** Default maximum number of random teleports per week. */
    public static final int DEFAULT_WEEKLY_LIMIT = 50;

    // -------------------------------------------------------------------------
    // RTP radius / attempts (RandomTeleportSettings)
    // -------------------------------------------------------------------------

    /** Default minimum RTP radius in blocks. */
    public static final int DEFAULT_MIN_RADIUS = 100;

    /** Default maximum RTP radius in blocks. */
    public static final int DEFAULT_MAX_RADIUS = 1000;

    /** Default maximum number of location-search attempts per teleport request. */
    public static final int DEFAULT_MAX_ATTEMPTS = 10;

    // -------------------------------------------------------------------------
    // Safety settings (SafetySettings)
    // -------------------------------------------------------------------------

    /** Default maximum blocks scanned upward/downward when recovering a player in the overworld. */
    public static final int SAFETY_DEFAULT_MAX_SURFACE_SCAN_DEPTH = 20;

    /** Default maximum blocks scanned when recovering a player in the Nether. */
    public static final int SAFETY_DEFAULT_MAX_SURFACE_SCAN_DEPTH_NETHER = 128;

    /** Hard ceiling on surface-scan depth to prevent runaway loops. */
    public static final int SAFETY_MAX_SURFACE_SCAN_DEPTH_LIMIT = 128;

    /** Default maximum number of blocks a player can be shifted vertically by the rescue mechanism. */
    public static final int SAFETY_DEFAULT_MAX_VERTICAL_RESCUE = 6;

    // -------------------------------------------------------------------------
    // Biome search settings (BiomeSearchSettings)
    // -------------------------------------------------------------------------

    /** Default wall-clock budget (ms) for common-biome searches; 0 = unlimited. */
    public static final int BIOME_SEARCH_DEFAULT_MAX_WALL_CLOCK_MILLIS = 0;

    /** Default wall-clock budget (ms) for rare-biome searches. */
    public static final int BIOME_SEARCH_DEFAULT_MAX_WALL_CLOCK_MILLIS_RARE = 5000;

    /** Default maximum biome-check rejections for common biomes; 0 = unlimited. */
    public static final int BIOME_SEARCH_DEFAULT_MAX_REJECTIONS = 0;

    /** Default maximum biome-check rejections for rare biomes. */
    public static final int BIOME_SEARCH_DEFAULT_MAX_REJECTIONS_RARE = 64;

    /** Default maximum chunk-loads per search for common biomes; 0 = unlimited. */
    public static final int BIOME_SEARCH_DEFAULT_MAX_CHUNK_LOADS = 0;

    /** Default maximum chunk-loads per search for rare biomes. */
    public static final int BIOME_SEARCH_DEFAULT_MAX_CHUNK_LOADS_RARE = 16;

    /** Default minimum biome attempts before a search can time out. */
    public static final int BIOME_SEARCH_DEFAULT_MIN_BIOME_ATTEMPTS = 96;

    /**
     * Tighter wall-clock budget (ms) applied automatically when include/exclude biome filters are
     * active but no explicit budget has been configured by the server operator.
     */
    public static final int BIOME_SEARCH_FILTER_AWARE_MAX_WALL_CLOCK_MILLIS = 100;

    /**
     * Tighter chunk-load cap applied automatically when include/exclude biome filters are active
     * but no explicit budget has been configured.
     */
    public static final int BIOME_SEARCH_FILTER_AWARE_MAX_CHUNK_LOADS = 10;

    // -------------------------------------------------------------------------
    // Biome pre-cache settings (BiomePreCacheSettings)
    // -------------------------------------------------------------------------

    /** Default maximum number of cached locations kept per biome. */
    public static final int BIOME_CACHE_DEFAULT_MAX_PER_BIOME = 25;

    /** Default number of locations populated during the initial warmup fill. */
    public static final int BIOME_CACHE_DEFAULT_WARMUP_SIZE = 5;

    /** Default number of minutes before a cached location entry expires. */
    public static final long BIOME_CACHE_DEFAULT_EXPIRATION_MINUTES = 15L;

    /** Default interval in minutes between cache refill passes. */
    public static final int BIOME_CACHE_DEFAULT_REFILL_INTERVAL_MINUTES = 15;

    /** Default number of locations added per cache refill batch. */
    public static final int BIOME_CACHE_DEFAULT_REFILL_BATCH_SIZE = 2;

    // -------------------------------------------------------------------------
    // Chunky integration settings (ChunkyIntegrationSettings)
    // -------------------------------------------------------------------------

    /** Default minimum free JVM heap (MB) required before triggering a Chunky pre-generation. */
    public static final long CHUNKY_DEFAULT_MIN_FREE_MEMORY_MB = 512L;

    /** Default maximum number of entries tracked in the Chunky coordinator. */
    public static final int CHUNKY_DEFAULT_MAX_COORDINATOR_ENTRIES = 10_000;

    /** Default number of minutes to retain Chunky coordinator entries under low-memory conditions. */
    public static final long CHUNKY_DEFAULT_LOW_MEMORY_RETENTION_MINUTES = 15L;
}
