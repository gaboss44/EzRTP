package com.skyblockexp.ezrtp.teleport.biome;

import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages a cache of pre-validated safe teleport locations organized by biome.
 * This cache helps improve success rates for biome-filtered random teleports by
 * storing locations that have already been validated as safe and matching specific biomes.
 * 
 * The cache automatically warms up in the background and periodically refreshes to
 * prevent memory buildup while maintaining good hit rates for common biomes.
 */
public final class BiomeLocationCache {
    
    public static final int DEFAULT_MAX_LOCATIONS_PER_BIOME = 50;
    public static final int DEFAULT_CACHE_WARMUP_SIZE = 20;
    public static final long DEFAULT_EXPIRATION_MINUTES = 10;
    private static final long WARMUP_DELAY_TICKS = 100L; // 5 seconds after startup
    private static final long WARMUP_BATCH_DELAY_TICKS = 2L; // Small delay between batches
    private static final int WARMUP_BATCH_SIZE = 5; // Process up to 5 chunks per batch
    private static final int DUPLICATE_LOCATION_DISTANCE_SQUARED = 100; // 10 blocks squared
    
    private final JavaPlugin plugin;
    private final ChunkyWarmupCoordinator chunkyCoordinator;
    private final Map<String, Map<Biome, Deque<CachedLocation>>> worldCaches;
    private final int maxLocationsPerBiome;
    private final long expirationMillis;
    private final int warmupSize;
    private BukkitTask warmupTask;
    private volatile boolean enabled;
    private final AtomicLong evictionCount = new AtomicLong(0);
    
    public BiomeLocationCache(JavaPlugin plugin) {
        this(plugin, DEFAULT_MAX_LOCATIONS_PER_BIOME, DEFAULT_CACHE_WARMUP_SIZE, DEFAULT_EXPIRATION_MINUTES, null);
    }
    
    public BiomeLocationCache(JavaPlugin plugin, int maxLocationsPerBiome, int warmupSize, long expirationMinutes) {
        this(plugin, maxLocationsPerBiome, warmupSize, expirationMinutes, null);
    }

    public BiomeLocationCache(JavaPlugin plugin, int maxLocationsPerBiome, int warmupSize, long expirationMinutes, ChunkyWarmupCoordinator chunkyCoordinator) {
        this.plugin = plugin;
        this.worldCaches = new ConcurrentHashMap<>();
        this.maxLocationsPerBiome = maxLocationsPerBiome;
        this.warmupSize = warmupSize;
        this.expirationMillis = expirationMinutes * 60 * 1000;
        this.enabled = true;
        this.chunkyCoordinator = chunkyCoordinator;
    }
    
    /**
     * Enables the cache. When disabled, all cache operations are no-ops.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            clear();
        }
    }
    
    /**
     * Returns whether the cache is currently enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Retrieves a cached location that matches the specified biomes.
     * Returns null if no matching location is found in the cache.
     * Expired locations are automatically removed.
     */
    public Location get(World world, Set<Biome> includeList, Set<Biome> excludeList) {
        if (!enabled || world == null) {
            return null;
        }
        
        Map<Biome, Deque<CachedLocation>> biomeCache = worldCaches.get(world.getName());
        if (biomeCache == null || biomeCache.isEmpty()) {
            return null;
        }
        
        // If no filters specified, we can't efficiently use the cache
        if ((includeList == null || includeList.isEmpty()) && (excludeList == null || excludeList.isEmpty())) {
            return null;
        }
        
        List<Biome> targetBiomes;
        if (includeList != null && !includeList.isEmpty()) {
            targetBiomes = new ArrayList<>(includeList);
        } else {
            // If only exclude list, we'd need to check all cached biomes - not efficient
            return null;
        }
        
        // Shuffle to provide variety and avoid always teleporting to the same location
        Collections.shuffle(targetBiomes);
        
        long now = System.currentTimeMillis();
        for (Biome biome : targetBiomes) {
            Deque<CachedLocation> locations = biomeCache.get(biome);
            if (locations == null || locations.isEmpty()) {
                continue;
            }
            // Try to find a valid, non-expired location
            Iterator<CachedLocation> it = locations.iterator();
            while (it.hasNext()) {
                CachedLocation cached = it.next();
                if (cached.isExpired(now)) {
                    it.remove();
                    continue;
                }
                if (excludeList != null && !excludeList.isEmpty() && excludeList.contains(biome)) {
                    it.remove();
                    continue;
                }
                // one-time use: remove and return
                it.remove();
                return cached.location();
            }
        }
        
        return null;
    }
    
    /**
     * Caches a validated location for its biome.
     * If the cache for this biome is full, the oldest entry is removed.
     */
    public void cache(Location location) {
        if (!enabled || location == null || location.getWorld() == null) {
            return;
        }
        
        Biome biome = location.getBlock().getBiome();
        String worldName = location.getWorld().getName();
        
        Map<Biome, Deque<CachedLocation>> biomeCache = worldCaches.computeIfAbsent(
            worldName, 
            k -> new ConcurrentHashMap<>()
        );
        
        Deque<CachedLocation> locations = biomeCache.computeIfAbsent(
            biome,
            k -> new ConcurrentLinkedDeque<>()
        );

        // Prevent duplicate locations (within 10 blocks)
        Location finalLocation = location;
        boolean isDuplicate = locations.stream().anyMatch(cached ->
            cached.location().distanceSquared(finalLocation) < DUPLICATE_LOCATION_DISTANCE_SQUARED
        );
        if (isDuplicate) return;

        // If cache is full, remove oldest entry
        while (locations.size() >= maxLocationsPerBiome) {
            locations.pollFirst();
            evictionCount.incrementAndGet();
        }

        locations.addLast(new CachedLocation(location.clone(), System.currentTimeMillis()));

        // Log cache addition with memory info (only at fine level to avoid spam)
        Runtime runtime = Runtime.getRuntime();
        long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / (1024L * 1024L);
        plugin.getLogger().fine(String.format(
            "[BiomeCache] Cached location for %s in world '%s' (total: %d, Memory: %dMB free)",
            biome.name(), worldName, locations.size(), freeMemoryMb
        ));
    }
    
    /**
     * Retrieves a random cached location from any biome in the specified world.
     * Returns null if no cached locations are available.
     * This is used as a fallback when RTP search fails.
     */
    public Location getRandomCachedLocation(World world) {
        if (!enabled || world == null) {
            return null;
        }
        
        Map<Biome, Deque<CachedLocation>> biomeCache = worldCaches.get(world.getName());
        if (biomeCache == null || biomeCache.isEmpty()) {
            return null;
        }
        
        // Collect all non-expired locations from all biomes
        List<Location> allValidLocations = new ArrayList<>();
        long now = System.currentTimeMillis();
        
        for (Deque<CachedLocation> locations : biomeCache.values()) {
            locations.removeIf(cached -> cached.isExpired(now));
            for (CachedLocation cached : locations) {
                allValidLocations.add(cached.location().clone());
            }
        }
        
        if (allValidLocations.isEmpty()) {
            return null;
        }
        
        // Return a random location from all valid cached locations
        // Using ThreadLocalRandom for better performance than creating new Random instance
        int randomIndex = java.util.concurrent.ThreadLocalRandom.current().nextInt(allValidLocations.size());
        return allValidLocations.get(randomIndex);
    }
    
    /**
     * Clears all cached locations for all worlds.
     */
    public void clear() {
        worldCaches.clear();
    }
    
    /**
     * Clears cached locations for a specific world.
     */
    public void clear(String worldName) {
        if (worldName != null) {
            worldCaches.remove(worldName);
        }
    }
    
    /**
     * Starts background cache warming for the specified world and settings.
     * This will asynchronously generate and cache locations to improve initial hit rates.
     */
    public void startWarmup(World world, com.skyblockexp.ezrtp.config.RandomTeleportSettings settings,
                           RandomTeleportService teleportService) {
        if (!enabled || world == null || settings == null || teleportService == null) {
            return;
        }
        
        // Only warm up if there are biome filters configured
        if (settings.getBiomeInclude().isEmpty() && settings.getBiomeExclude().isEmpty()) {
            return;
        }
        
        // Cancel any existing warmup task
        if (warmupTask != null && !warmupTask.isCancelled()) {
            warmupTask.cancel();
        }
        
        // Schedule warmup task with a delay to avoid startup lag
        warmupTask = Bukkit.getScheduler().runTaskLater(plugin,
            () -> performWarmup(world, settings, teleportService, 0),
            WARMUP_DELAY_TICKS
        );
    }
    
    private void performWarmup(World world, com.skyblockexp.ezrtp.config.RandomTeleportSettings settings,
                              RandomTeleportService teleportService, int completedCount) {
        // Check memory before starting warmup
        Runtime runtime = Runtime.getRuntime();
        long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / (1024L * 1024L);
        long totalMemoryMb = runtime.totalMemory() / (1024L * 1024L);
        
        if (freeMemoryMb < 256) { // Less than 256MB free memory
            plugin.getLogger().warning(String.format(
                "[BiomeCache] Skipping warmup for world '%s' due to low memory: %dMB free / %dMB total",
                world.getName(), freeMemoryMb, totalMemoryMb
            ));
            return;
        }
        
        if (!enabled || completedCount >= warmupSize) {
            if (enabled && completedCount > 0) {
                plugin.getLogger().info(String.format(
                    "[BiomeCache] Warmed up %d locations for world '%s' (Memory: %dMB free / %dMB total)",
                    completedCount, world.getName(), freeMemoryMb, totalMemoryMb
                ));
            }
            return;
        }
        
        plugin.getLogger().fine(String.format(
            "[BiomeCache] Starting warmup batch for world '%s' (completed: %d/%d, Memory: %dMB free / %dMB total)",
            world.getName(), completedCount, warmupSize, freeMemoryMb, totalMemoryMb
        ));
        
        // If ChunkyWarmupCoordinator indicates planned/generated chunks, prioritize those first
        if (chunkyCoordinator != null) {
            Set<Long> chunks = chunkyCoordinator.getChunksForWorld(world.getName());
            if (chunks != null && !chunks.isEmpty()) {
                int batchSize = Math.min(WARMUP_BATCH_SIZE, Math.min(warmupSize - completedCount, chunks.size()));
                
                // Check if we have enough memory for batch processing
                long memoryPerLocationEstimate = 50; // Rough estimate of memory per location in KB
                long estimatedMemoryKb = batchSize * memoryPerLocationEstimate;
                long freeMemoryKb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1024L;
                
                int effectiveBatchSize = batchSize;
                if (freeMemoryKb < estimatedMemoryKb * 2) { // Need at least 2x the estimated memory
                    effectiveBatchSize = Math.max(1, (int)(freeMemoryKb / (memoryPerLocationEstimate * 2)));
                    plugin.getLogger().info(String.format(
                        "[BiomeCache] Reducing warmup batch size to %d due to memory constraints (%dKB free)",
                        effectiveBatchSize, freeMemoryKb
                    ));
                }
                
                plugin.getLogger().fine(String.format(
                    "[BiomeCache] Processing %d chunk-based warmup locations for world '%s' (Memory: %dMB free)",
                    effectiveBatchSize, world.getName(), freeMemoryMb
                ));
                
                List<CompletableFuture<Location>> futures = new ArrayList<>();
                Iterator<Long> iterator = chunks.iterator();
                for (int i = 0; i < effectiveBatchSize && iterator.hasNext(); i++) {
                    Long key = iterator.next();
                    int chunkX = (int) (key >> 32);
                    int chunkZ = key.intValue();
                    // Attempt to generate a location centered in this chunk
                    CompletableFuture<Location> future = teleportService.generateSafeLocationForChunk(world, chunkX, chunkZ, settings);
                    futures.add(future);
                    future.thenAccept(location -> {
                        if (location != null) {
                            cache(location);
                            if (chunkyCoordinator != null) chunkyCoordinator.recordWarmupProcessed();
                        }
                    });
                }
                // Wait for all in batch to complete, then schedule next
                final int batchSnapshot = effectiveBatchSize;
                final int completedSnapshot = completedCount;
                CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allOf.thenRun(() -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        int newCompleted = completedSnapshot + batchSnapshot;
                        if (newCompleted < warmupSize) {
                            Bukkit.getScheduler().runTaskLater(plugin,
                                () -> performWarmup(world, settings, teleportService, newCompleted),
                                WARMUP_BATCH_DELAY_TICKS
                            );
                        } else {
                            plugin.getLogger().info(String.format(
                                "[BiomeCache] Warmed up %d locations for world '%s'",
                                warmupSize, world.getName()
                            ));
                        }
                    });
                });
                return;
            }
        }
        
        // Fallback: process one at a time for random warmup
        warmupAttempt(world, settings, teleportService, () -> {
            int newCompleted = completedCount + 1;
            if (newCompleted < warmupSize) {
                Bukkit.getScheduler().runTaskLater(plugin,
                    () -> performWarmup(world, settings, teleportService, newCompleted),
                    WARMUP_BATCH_DELAY_TICKS
                );
            } else {
                plugin.getLogger().info(String.format(
                    "[BiomeCache] Warmed up %d locations for world '%s'",
                    warmupSize, world.getName()
                ));
            }
        });
    }
    
    /**
     * Generates a single safe location for cache warmup.
     * Calls the onSuccess callback when a location is successfully cached.
     */
    private void warmupAttempt(World world, com.skyblockexp.ezrtp.config.RandomTeleportSettings settings,
                              RandomTeleportService teleportService, Runnable onSuccess) {
        // Check memory before attempting warmup
        Runtime runtime = Runtime.getRuntime();
        long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / (1024L * 1024L);
        
        if (freeMemoryMb < 128) { // Critically low memory
            plugin.getLogger().warning(String.format(
                "[BiomeCache] Aborting warmup attempt for world '%s' due to critically low memory: %dMB free",
                world.getName(), freeMemoryMb
            ));
            return;
        }
        
        plugin.getLogger().fine(String.format(
            "[BiomeCache] Attempting warmup location for world '%s' (Memory: %dMB free)",
            world.getName(), freeMemoryMb
        ));
        
        // Use the teleport service to generate a safe location for caching
        teleportService.generateSafeLocationForCache(world, settings).thenAccept(location -> {
            if (location != null) {
                cache(location);
                plugin.getLogger().fine(String.format(
                    "[BiomeCache] Successfully cached location at %s in world '%s'",
                    location.toString(), world.getName()
                ));
                // Run the success callback on the main thread
                try {
                    Bukkit.getScheduler().runTask(plugin, onSuccess);
                } catch (IllegalStateException ex) {
                    // Plugin is shutting down, log warning to indicate warmup was interrupted
                    if (plugin.isEnabled()) {
                        plugin.getLogger().warning("[BiomeCache] Warmup interrupted - plugin may be shutting down");
                    }
                }
            } else {
                plugin.getLogger().fine(String.format(
                    "[BiomeCache] Failed to generate warmup location for world '%s'",
                    world.getName()
                ));
            }
        });
    }
    
    /**
     * Returns all cached locations for a specific world.
     * Returns an empty list if the world is not cached or if the cache is disabled.
     * This method removes expired locations during traversal.
     */
    public java.util.List<Location> getAllLocations(String worldName) {
        if (!enabled || worldName == null) {
            return java.util.Collections.emptyList();
        }
        
        Map<Biome, Deque<CachedLocation>> biomeCache = worldCaches.get(worldName);
        if (biomeCache == null || biomeCache.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        java.util.List<Location> allLocations = new java.util.ArrayList<>();
        long now = System.currentTimeMillis();
        
        for (Deque<CachedLocation> locations : biomeCache.values()) {
            locations.removeIf(cached -> cached.isExpired(now));
            for (CachedLocation cached : locations) {
                allLocations.add(cached.location().clone());
            }
        }
        
        return allLocations;
    }

    /**
     * Returns all cached locations for a specific biome in a specific world.
     * Returns an empty list if the world or biome is not cached or if the cache is disabled.
     * This method removes expired locations during traversal.
     */
    public java.util.List<Location> getLocations(String worldName, Biome biome) {
        if (!enabled || worldName == null || biome == null) {
            return java.util.Collections.emptyList();
        }
        Map<Biome, Deque<CachedLocation>> biomeCache = worldCaches.get(worldName);
        if (biomeCache == null || biomeCache.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        Deque<CachedLocation> cached = biomeCache.get(biome);
        if (cached == null || cached.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        java.util.List<Location> result = new java.util.ArrayList<>();
        long now = System.currentTimeMillis();
        cached.removeIf(c -> c.isExpired(now));
        for (CachedLocation c : cached) {
            result.add(c.location().clone());
        }
        return result;
    }
    
    /**
     * Returns statistics about the current cache state.
     */
    public CacheStats getStats() {
        int totalLocations = 0;
        int totalBiomes = 0;

        for (Map<Biome, Deque<CachedLocation>> worldCache : worldCaches.values()) {
            totalBiomes += worldCache.size();
            for (Deque<CachedLocation> locations : worldCache.values()) {
                totalLocations += locations.size();
            }
        }

        return new CacheStats(worldCaches.size(), totalBiomes, totalLocations, evictionCount.get());
    }

    public long getEvictionCount() {
        return evictionCount.get();
    }
    
    /**
     * Returns the count of cached locations for the specified world and biomes.
     * This method counts non-expired locations that match any of the provided biomes.
     * 
     * @param world The world to check
     * @param biomes The set of biomes to count locations for
     * @return The total count of cached locations, or 0 if none are available
     */
    public int getCachedLocationCount(World world, Set<Biome> biomes) {
        if (!enabled || world == null || biomes == null || biomes.isEmpty()) {
            return 0;
        }
        
        Map<Biome, Deque<CachedLocation>> biomeCache = worldCaches.get(world.getName());
        if (biomeCache == null || biomeCache.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        long now = System.currentTimeMillis();
        
        for (Biome biome : biomes) {
            Deque<CachedLocation> locations = biomeCache.get(biome);
            if (locations == null || locations.isEmpty()) continue;
            for (CachedLocation cached : locations) if (!cached.isExpired(now)) count++;
        }
        
        return count;
    }
    
    /**
     * Shuts down the cache and cancels any pending warmup tasks.
     */
    public void shutdown() {
        if (warmupTask != null && !warmupTask.isCancelled()) {
            warmupTask.cancel();
        }
        clear();
        enabled = false;
    }
    
    /**
     * Represents a cached location with its timestamp.
     */
    private class CachedLocation {
        private final Location location;
        private final long timestamp;
        
        CachedLocation(Location location, long timestamp) {
            this.location = location;
            this.timestamp = timestamp;
        }
        
        Location location() {
            return location;
        }
        
        boolean isExpired(long currentTime) {
            return (currentTime - timestamp) > expirationMillis;
        }
    }
    
    /**
     * Statistics about the cache state.
     */
    public record CacheStats(int worldCount, int biomeCount, int locationCount, long evictions) {
        @Override
        public String toString() {
            return String.format("CacheStats[worlds=%d, biomes=%d, locations=%d, evictions=%d]",
                worldCount, biomeCount, locationCount, evictions);
        }
    }
}
