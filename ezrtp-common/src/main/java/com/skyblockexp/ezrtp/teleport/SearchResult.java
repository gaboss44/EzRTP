package com.skyblockexp.ezrtp.teleport;

import org.bukkit.Location;

import java.util.Optional;

/**
 * Represents the result of a location search for a random teleport.
 */
public record SearchResult(Optional<Location> location,
                           boolean noValidBiome,
                           boolean cacheHit,
                           boolean cacheChecked,
                           boolean fallbackUsed,
                           boolean noCacheAvailable,
                           SearchLimitType limitType) {

    public enum SearchLimitType {
        NONE,
        WALL_CLOCK,
        BIOME_REJECTIONS,
        CHUNK_LOADS
    }

    public SearchResult(Optional<Location> location, boolean noValidBiome, boolean cacheHit) {
        this(location, noValidBiome, cacheHit, cacheHit, false, false, SearchLimitType.NONE);
    }

    public SearchResult(Optional<Location> location,
                        boolean noValidBiome,
                        boolean cacheHit,
                        boolean fallbackUsed) {
        this(location, noValidBiome, cacheHit, true, fallbackUsed, false, SearchLimitType.NONE);
    }
}
