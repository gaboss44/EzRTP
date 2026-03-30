package com.skyblockexp.ezrtp.teleport.search;

import org.bukkit.World;
import org.bukkit.block.Biome;

import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;

import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;

/**
 * Generates coordinates by randomly selecting points within a square annulus.
 */
public final class SquareSearchStrategy implements BiomeSearchStrategy {

    

    @Override
    public int[] generateCandidateCoordinates(World world, int centerX, int centerZ,
                                              int minRadius, int maxRadius,
                                              Set<Biome> targetBiomes,
                                              RareBiomeRegistry registry) {
        int effectiveMin = Math.min(minRadius, maxRadius);
        int effectiveMax = Math.max(minRadius, maxRadius);
        if (effectiveMax <= 0 && effectiveMin <= 0) {
            return new int[]{centerX, centerZ};
        }

        int radius = ThreadLocalRandom.current().nextInt(effectiveMax - effectiveMin + 1) + effectiveMin;
        // Pick a random edge: 0=top, 1=right, 2=bottom, 3=left
        int edge = ThreadLocalRandom.current().nextInt(4);
        int offset = ThreadLocalRandom.current().nextInt(radius * 2 + 1);
        int localX = 0, localZ = 0;
        switch (edge) {
            case 0 -> { // Top edge: left to right
                localX = -radius + offset;
                localZ = -radius;
            }
            case 1 -> { // Right edge: top to bottom
                localX = radius;
                localZ = -radius + offset;
            }
            case 2 -> { // Bottom edge: right to left
                localX = radius - offset;
                localZ = radius;
            }
            case 3 -> { // Left edge: bottom to top
                localX = -radius;
                localZ = radius - offset;
            }
        }
        return new int[]{centerX + localX, centerZ + localZ};
    }

    @Override
    public String getStrategyName() {
        return "square";
    }

    @Override
    public boolean isApplicableFor(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
        return true;
    }
}
