package com.skyblockexp.ezrtp.teleport.heatmap;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

/**
 * Stores short-lived simulated heatmap points that are injected via 
 * administrative commands. The data never hits disk and is only meant 
 * to help visualize distribution patterns when running diagnostics.
 */
public final class HeatmapSimulationStore {

    private static final int DEFAULT_CAPACITY = 5000;

    private final Map<String, ConcurrentLinkedDeque<Location>> samplesPerWorld = new ConcurrentHashMap<>();
    private final int perWorldCapacity;

    public HeatmapSimulationStore() {
        this(DEFAULT_CAPACITY);
    }

    public HeatmapSimulationStore(int perWorldCapacity) {
        this.perWorldCapacity = Math.max(1, perWorldCapacity);
    }

    public int getPerWorldCapacity() {
        return perWorldCapacity;
    }

    /**
     * Adds the provided samples to the in-memory buffer and returns the number of
     * samples that were actually enqueued. Extra entries beyond the configured
     * per-world capacity are discarded from the oldest side of the buffer.
     *
     * <p>Uses a {@link ConcurrentLinkedDeque} directly to avoid copying the entire
     * deque on every call. Overflow trimming is best-effort under concurrent access;
     * the buffer may briefly exceed capacity by the size of a single batch.
     */
    public int addSamples(String worldName, Collection<Location> samples) {
        if (worldName == null || samples == null || samples.isEmpty()) {
            return 0;
        }
        final String key = normalize(worldName);
        final List<Location> cleanSamples = samples.stream()
            .filter(Objects::nonNull)
            .map(Location::clone)
            .collect(Collectors.toList());
        if (cleanSamples.isEmpty()) {
            return 0;
        }
        ConcurrentLinkedDeque<Location> buffer = samplesPerWorld.computeIfAbsent(
            key, k -> new ConcurrentLinkedDeque<>());
        for (Location sample : cleanSamples) {
            buffer.addLast(sample);
        }
        // Trim oldest entries to stay within per-world capacity.
        // ConcurrentLinkedDeque.size() is O(n); we accept this since addSamples is
        // an infrequent admin operation, not a hot teleport path.
        while (buffer.size() > perWorldCapacity) {
            buffer.pollFirst();
        }
        return cleanSamples.size();
    }

    /**
     * Returns a defensive copy of the current samples for the given world. The
     * returned list is safe to modify by the caller.
     */
    public List<Location> getSamples(String worldName) {
        if (worldName == null) {
            return Collections.emptyList();
        }
        ConcurrentLinkedDeque<Location> buffer = samplesPerWorld.get(normalize(worldName));
        if (buffer == null || buffer.isEmpty()) {
            return Collections.emptyList();
        }
        List<Location> copy = new ArrayList<>(buffer.size());
        for (Location location : buffer) {
            copy.add(location.clone());
        }
        return copy;
    }

    /**
     * Removes every simulated point for the specified world and returns the
     * number of entries that were cleared.
     */
    public int clearWorld(String worldName) {
        if (worldName == null) {
            return 0;
        }
        ConcurrentLinkedDeque<Location> removed = samplesPerWorld.remove(normalize(worldName));
        return removed != null ? removed.size() : 0;
    }

    /** Clears the entire simulation store. */
    public void clearAll() {
        samplesPerWorld.clear();
    }

    private static String normalize(String worldName) {
        return worldName.toLowerCase(Locale.ROOT);
    }
}
