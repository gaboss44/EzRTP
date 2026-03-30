package com.skyblockexp.ezrtp.teleport.biome;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry for tracking and managing rare biome hotspots.
 * Maintains a mapping of known rare biome locations to optimize RTP searches.
 * Periodically scans for new rare biome hotspots during idle server times.
 */
public final class RareBiomeRegistry {
    
    private static final long BACKGROUND_SCAN_INTERVAL_TICKS = 6000L; // 5 minutes
    private static final int MAX_HOTSPOTS_PER_BIOME = 100;
    private static final int SCAN_BATCH_SIZE = 5; // Number of locations to scan per tick
    private static final int HOTSPOT_RADIUS = 256; // Minimum distance between hotspots
    
    private final JavaPlugin plugin;
    private final Map<String, Map<Biome, List<BiomeHotspot>>> worldHotspots;
    private final Set<Biome> rareBiomes;
    private BukkitTask backgroundScanTask;
    private volatile boolean enabled;
    private volatile boolean backgroundScanningEnabled;
    
    public RareBiomeRegistry(JavaPlugin plugin, Set<Biome> rareBiomes) {
        this.plugin = plugin;
        this.worldHotspots = new ConcurrentHashMap<>();
        this.rareBiomes = rareBiomes != null ? new HashSet<>(rareBiomes) : getDefaultRareBiomes();
        this.enabled = true;
        this.backgroundScanningEnabled = true;
    }
    
    /**
     * Returns the default set of rare biomes if none are configured.
     */
    private static Set<Biome> getDefaultRareBiomes() {
        Set<Biome> rare = new HashSet<>();
        // Rare biomes that are typically hard to find
        try {
            rare.add(Biome.valueOf("MUSHROOM_FIELDS"));
            rare.add(Biome.valueOf("JUNGLE"));
            rare.add(Biome.valueOf("BAMBOO_JUNGLE"));
            rare.add(Biome.valueOf("BADLANDS"));
            rare.add(Biome.valueOf("ERODED_BADLANDS"));
            rare.add(Biome.valueOf("WOODED_BADLANDS"));
            rare.add(Biome.valueOf("ICE_SPIKES"));
            rare.add(Biome.valueOf("SUNFLOWER_PLAINS"));
            rare.add(Biome.valueOf("FLOWER_FOREST"));
            rare.add(Biome.valueOf("MODIFIED_JUNGLE"));
            rare.add(Biome.valueOf("MODIFIED_JUNGLE_EDGE"));
            rare.add(Biome.valueOf("DEEP_DARK"));
        } catch (IllegalArgumentException e) {
            // Some biomes might not exist in all versions
        }
        return rare;
    }
    
    /**
     * Enables or disables the registry.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            stopBackgroundScanning();
            clear();
        }
    }
    
    /**
     * Returns whether the registry is currently enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Enables or disables background scanning.
     */
    public void setBackgroundScanningEnabled(boolean enabled) {
        this.backgroundScanningEnabled = enabled;
        if (enabled) {
            startBackgroundScanning();
        } else {
            stopBackgroundScanning();
        }
    }
    
    /**
     * Returns whether background scanning is enabled.
     */
    public boolean isBackgroundScanningEnabled() {
        return backgroundScanningEnabled;
    }
    
    /**
     * Checks if a biome is considered rare.
     */
    public boolean isRareBiome(Biome biome) {
        return rareBiomes.contains(biome);
    }
    
    /**
     * Registers a rare biome hotspot location.
     */
    public void registerHotspot(Location location) {
        if (!enabled || location == null || location.getWorld() == null) {
            return;
        }
        
        Biome biome = location.getBlock().getBiome();
        if (!isRareBiome(biome)) {
            return;
        }
        
        String worldName = location.getWorld().getName();
        Map<Biome, List<BiomeHotspot>> hotspots = worldHotspots.computeIfAbsent(
            worldName,
            k -> new ConcurrentHashMap<>()
        );
        
        List<BiomeHotspot> biomeHotspots = hotspots.computeIfAbsent(
            biome,
            k -> new CopyOnWriteArrayList<>()
        );
        
        synchronized (biomeHotspots) {
            // Check if this location is too close to an existing hotspot
            Location finalLocation = location;
            boolean tooClose = biomeHotspots.stream()
                .anyMatch(hs -> hs.location().distanceSquared(finalLocation) < HOTSPOT_RADIUS * HOTSPOT_RADIUS);
            
            if (tooClose) {
                return;
            }
            
            // If we've reached the maximum, remove the oldest hotspot
            if (biomeHotspots.size() >= MAX_HOTSPOTS_PER_BIOME) {
                biomeHotspots.remove(0);
            }
            
            biomeHotspots.add(new BiomeHotspot(location.clone(), System.currentTimeMillis()));
        }
    }
    
    /**
     * Retrieves all hotspot locations for a specific rare biome in the given world.
     * Returns an empty list if no hotspots are registered.
     */
    public List<Location> getHotspots(World world, Biome biome) {
        if (!enabled || world == null || !isRareBiome(biome)) {
            return Collections.emptyList();
        }
        
        Map<Biome, List<BiomeHotspot>> hotspots = worldHotspots.get(world.getName());
        if (hotspots == null) {
            return Collections.emptyList();
        }
        
        List<BiomeHotspot> biomeHotspots = hotspots.get(biome);
        if (biomeHotspots == null || biomeHotspots.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Return a shuffled copy to provide variety
        List<Location> locations = biomeHotspots.stream()
            .map(BiomeHotspot::location)
            .toList();
        List<Location> shuffled = new ArrayList<>(locations);
        Collections.shuffle(shuffled);
        return shuffled;
    }
    
    /**
     * Gets the total count of registered hotspots for a biome.
     */
    public int getHotspotCount(World world, Biome biome) {
        if (!enabled || world == null || !isRareBiome(biome)) {
            return 0;
        }
        
        Map<Biome, List<BiomeHotspot>> hotspots = worldHotspots.get(world.getName());
        if (hotspots == null) {
            return 0;
        }
        
        List<BiomeHotspot> biomeHotspots = hotspots.get(biome);
        return biomeHotspots != null ? biomeHotspots.size() : 0;
    }
    
    /**
     * Starts background scanning for rare biome hotspots during idle server times.
     */
    public void startBackgroundScanning() {
        if (!enabled || !backgroundScanningEnabled) {
            return;
        }
        
        // Cancel any existing task
        stopBackgroundScanning();
        
        // Schedule periodic background scanning
        backgroundScanTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!enabled || !backgroundScanningEnabled) {
                return;
            }
            
            // Only scan during low server load (TPS check could be added here)
            performBackgroundScan();
        }, BACKGROUND_SCAN_INTERVAL_TICKS, BACKGROUND_SCAN_INTERVAL_TICKS);
    }
    
    /**
     * Stops background scanning.
     */
    public void stopBackgroundScanning() {
        if (backgroundScanTask != null && !backgroundScanTask.isCancelled()) {
            backgroundScanTask.cancel();
            backgroundScanTask = null;
        }
    }
    
    /**
     * Performs a background scan for rare biomes in loaded chunks.
     * This method is designed to be lightweight and spread across multiple ticks.
     */
    private void performBackgroundScan() {
        // Get all online worlds
        List<World> worlds = Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .toList();
        
        if (worlds.isEmpty()) {
            return;
        }
        
        // Pick a random world to scan
        World world = worlds.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(worlds.size()));
        
        // Scan a few random loaded chunks for rare biomes
        var loadedChunks = world.getLoadedChunks();
        if (loadedChunks.length == 0) {
            return;
        }
        
        var random = java.util.concurrent.ThreadLocalRandom.current();
        int scanned = 0;
        int maxScans = Math.min(SCAN_BATCH_SIZE, loadedChunks.length);
        
        while (scanned < maxScans) {
            var chunk = loadedChunks[random.nextInt(loadedChunks.length)];
            int x = chunk.getX() << 4;
            int z = chunk.getZ() << 4;
            
            // Sample a few locations in this chunk
            for (int dx = 0; dx < 16; dx += 8) {
                for (int dz = 0; dz < 16; dz += 8) {
                    Location loc = world.getHighestBlockAt(x + dx, z + dz).getLocation();
                    Biome biome = loc.getBlock().getBiome();
                    
                    if (isRareBiome(biome)) {
                        registerHotspot(loc);
                    }
                }
            }
            scanned++;
        }
    }
    
    /**
     * Clears all registered hotspots.
     */
    public void clear() {
        worldHotspots.clear();
    }
    
    /**
     * Clears hotspots for a specific world.
     */
    public void clear(String worldName) {
        if (worldName != null) {
            worldHotspots.remove(worldName);
        }
    }
    
    /**
     * Returns statistics about registered hotspots.
     */
    public RegistryStats getStats() {
        int totalHotspots = 0;
        int totalBiomes = 0;
        Map<Biome, Integer> biomeDistribution = new HashMap<>();
        
        for (Map<Biome, List<BiomeHotspot>> worldMap : worldHotspots.values()) {
            totalBiomes += worldMap.size();
            for (Map.Entry<Biome, List<BiomeHotspot>> entry : worldMap.entrySet()) {
                int count = entry.getValue().size();
                totalHotspots += count;
                biomeDistribution.merge(entry.getKey(), count, Integer::sum);
            }
        }
        
        return new RegistryStats(
            worldHotspots.size(),
            totalBiomes,
            totalHotspots,
            rareBiomes.size(),
            biomeDistribution
        );
    }
    
    /**
     * Shuts down the registry and cancels all tasks.
     */
    public void shutdown() {
        stopBackgroundScanning();
        clear();
        enabled = false;
    }
    
    /**
     * Represents a registered rare biome hotspot.
     */
    private record BiomeHotspot(Location location, long timestamp) {}
    
    /**
     * Statistics about the registry state.
     */
    public record RegistryStats(
        int worldCount,
        int biomeCount,
        int hotspotCount,
        int rareBiomeCount,
        Map<Biome, Integer> biomeDistribution
    ) {
        @Override
        public String toString() {
            return String.format("RegistryStats[worlds=%d, biomes=%d, hotspots=%d, rareBiomes=%d]",
                worldCount, biomeCount, hotspotCount, rareBiomeCount);
        }
    }
}
