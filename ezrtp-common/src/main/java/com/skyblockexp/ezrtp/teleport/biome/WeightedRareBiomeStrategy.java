package com.skyblockexp.ezrtp.teleport.biome;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import com.skyblockexp.ezrtp.teleport.search.BiomeSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.SamplingUtil;
import com.skyblockexp.ezrtp.teleport.search.UniformSearchStrategy;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;

/**
 * Weighted search strategy for rare biomes.
 * Prioritizes searching near known rare biome hotspots while still maintaining randomness.
 */
public final class WeightedRareBiomeStrategy implements BiomeSearchStrategy {
    
    private static final double HOTSPOT_WEIGHT = 0.7; // 70% chance to search near a hotspot
    private static final int HOTSPOT_SEARCH_RADIUS = 512; // Search within 512 blocks of a hotspot
    
    
    private final BiomeSearchStrategy fallbackStrategy;

    public WeightedRareBiomeStrategy() {
        this(new UniformSearchStrategy());
    }

    public WeightedRareBiomeStrategy(BiomeSearchStrategy fallbackStrategy) {
        this.fallbackStrategy = fallbackStrategy != null ? fallbackStrategy : new UniformSearchStrategy();
    }
    
    @Override
    public int[] generateCandidateCoordinates(World world, int centerX, int centerZ,
                                             int minRadius, int maxRadius,
                                             Set<Biome> targetBiomes,
                                             RareBiomeRegistry registry) {
        // If no rare biomes in target list, use uniform strategy
        if (!hasRareBiomes(targetBiomes, registry)) {
            return fallbackStrategy.generateCandidateCoordinates(world, centerX, centerZ,
                minRadius, maxRadius, targetBiomes, registry);
        }
        
        // Try to use hotspot-based search with a certain probability
        if (ThreadLocalRandom.current().nextDouble() < HOTSPOT_WEIGHT) {
            // Find hotspots for any of the target rare biomes
            for (Biome biome : targetBiomes) {
                if (registry.isRareBiome(biome)) {
                    List<Location> hotspots = registry.getHotspots(world, biome);
                    if (!hotspots.isEmpty()) {
                        // Pick a random hotspot
                        Location hotspot = hotspots.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(hotspots.size()));

                        // Generate coordinates near this hotspot
                        int offsetRadius = SamplingUtil.randomIntInclusive(0, HOTSPOT_SEARCH_RADIUS - 1);
                        double angle = SamplingUtil.randomAngle();
                        
                        int x = hotspot.getBlockX() + (int) Math.round(Math.cos(angle) * offsetRadius);
                        int z = hotspot.getBlockZ() + (int) Math.round(Math.sin(angle) * offsetRadius);
                        
                        // Ensure we're still within the configured search bounds
                        double distFromCenter = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));
                        if (distFromCenter >= minRadius && distFromCenter <= maxRadius) {
                            return new int[]{x, z};
                        }
                    }
                }
            }
        }
        
        // Fall back to uniform search if no hotspots found or random chance dictates
        return fallbackStrategy.generateCandidateCoordinates(world, centerX, centerZ,
            minRadius, maxRadius, targetBiomes, registry);
    }
    
    @Override
    public String getStrategyName() {
        return "weighted-rare-biome";
    }
    
    @Override
    public boolean isApplicableFor(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
        return hasRareBiomes(targetBiomes, registry);
    }
    
    /**
     * Checks if any of the target biomes are rare biomes.
     */
    private boolean hasRareBiomes(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
        if (targetBiomes == null || targetBiomes.isEmpty() || registry == null) {
            return false;
        }
        return targetBiomes.stream().anyMatch(registry::isRareBiome);
    }
}
