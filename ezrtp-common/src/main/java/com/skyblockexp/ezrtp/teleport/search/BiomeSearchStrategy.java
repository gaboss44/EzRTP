package com.skyblockexp.ezrtp.teleport.search;

import org.bukkit.World;
import org.bukkit.block.Biome;

import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;

import java.util.Set;

/**
 * Strategy interface for different biome search approaches.
 * Allows for different search algorithms to be used based on the target biomes.
 */
public interface BiomeSearchStrategy {
    
    /**
     * Generates candidate coordinates for a location search.
     * 
     * @param world The world to search in
     * @param centerX Center X coordinate
     * @param centerZ Center Z coordinate
     * @param minRadius Minimum search radius
     * @param maxRadius Maximum search radius
     * @param targetBiomes Target biomes to search for (may be empty)
     * @param registry Rare biome registry for hotspot data
     * @return Suggested coordinates [x, z]
     */
    int[] generateCandidateCoordinates(World world, int centerX, int centerZ,
                                       int minRadius, int maxRadius,
                                       Set<Biome> targetBiomes,
                                       RareBiomeRegistry registry);
    
    /**
     * Returns the name of this strategy for logging and statistics.
     */
    String getStrategyName();
    
    /**
     * Returns whether this strategy should be used for the given biome set.
     */
    boolean isApplicableFor(Set<Biome> targetBiomes, RareBiomeRegistry registry);
}
