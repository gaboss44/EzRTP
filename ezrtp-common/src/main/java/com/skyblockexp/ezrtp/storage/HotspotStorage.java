package com.skyblockexp.ezrtp.storage;

import org.bukkit.block.Biome;

import java.util.List;

/**
 * Persistence contract for rare-biome hotspot locations.
 *
 * <p>Hotspots are expensive to discover (they require traversing loaded chunks or running RTP
 * searches). Persisting them across restarts and sharing them between servers significantly
 * reduces cold-start discovery time and removes the need for each node to re-scan independently.
 */
public interface HotspotStorage {

    /**
     * Persists a single hotspot location.
     *
     * @param world world name
     * @param biome biome enum name ({@link Biome#name()})
     * @param x     block X coordinate
     * @param y     block Y coordinate
     * @param z     block Z coordinate
     */
    void saveHotspot(String world, String biome, double x, double y, double z);

    /**
     * Loads all stored hotspot records. Intended to be called once at plugin startup on a
     * background thread before scanning begins.
     *
     * @return list of all persisted hotspot records (may be empty, never null)
     */
    List<HotspotRecord> loadAll();

    /** Releases any resources held by this storage (e.g. connection pool). */
    void close();

    /**
     * A single persisted hotspot entry.
     *
     * @param world     world name
     * @param biome     biome enum name
     * @param x         block X coordinate
     * @param y         block Y coordinate
     * @param z         block Z coordinate
     * @param timestamp milliseconds since epoch when the hotspot was first registered
     */
    record HotspotRecord(String world, String biome, double x, double y, double z, long timestamp) {}
}
