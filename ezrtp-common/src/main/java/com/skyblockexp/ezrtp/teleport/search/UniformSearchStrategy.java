package com.skyblockexp.ezrtp.teleport.search;

import org.bukkit.World;
import org.bukkit.block.Biome;

import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;

import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;

/**
 * Standard uniform random search strategy.
 * Generates locations uniformly across the search radius.
 */
public final class UniformSearchStrategy implements BiomeSearchStrategy {
    
    
    
    @Override
    public int[] generateCandidateCoordinates(World world, int centerX, int centerZ,
                                             int minRadius, int maxRadius,
                                             Set<Biome> targetBiomes,
                                             RareBiomeRegistry registry) {
        int radius = SamplingUtil.randomIntInclusive(minRadius, maxRadius);
        double angle = SamplingUtil.randomAngle();
        
        int x = centerX + (int) Math.round(Math.cos(angle) * radius);
        int z = centerZ + (int) Math.round(Math.sin(angle) * radius);
        
        return new int[]{x, z};
    }
    
    @Override
    public String getStrategyName() {
        return "uniform";
    }
    
    @Override
    public boolean isApplicableFor(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
        // Always applicable as the fallback strategy
        return true;
    }
}
