package com.skyblockexp.ezrtp.teleport.search;

import org.bukkit.World;
import org.bukkit.block.Biome;

import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;

import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;

/**
 * Generates coordinates by selecting a random point on the perimeter of an equilateral triangle
 * centered on the configured center point. The triangle is defined by the requested radius.
 */
public final class TriangleSearchStrategy implements BiomeSearchStrategy {

    

    @Override
    public int[] generateCandidateCoordinates(World world, int centerX, int centerZ,
                                              int minRadius, int maxRadius,
                                              Set<Biome> targetBiomes,
                                              RareBiomeRegistry registry) {
        int effectiveMin = Math.min(minRadius, maxRadius);
        int effectiveMax = Math.max(minRadius, maxRadius);

        // If radius range collapses to 0, return center
        if (effectiveMax <= 0 && effectiveMin <= 0) {
            return new int[]{centerX, centerZ};
        }

        // Choose radius within range
        int radius = effectiveMin == effectiveMax ? effectiveMin : SamplingUtil.randomIntInclusive(effectiveMin, effectiveMax);

        // Compute the 3 triangle vertices (equilateral) around the center.
        // Start with a vertex pointing north (-90 degrees) and then place remaining vertices 120deg apart.
        double[] vx = new double[3];
        double[] vz = new double[3];
        for (int i = 0; i < 3; i++) {
            double angle = -Math.PI / 2.0 + i * 2.0 * Math.PI / 3.0;
            vx[i] = Math.cos(angle) * radius;
            vz[i] = Math.sin(angle) * radius;
        }

        // Pick one of the three edges
        int edge = ThreadLocalRandom.current().nextInt(3);
        int next = (edge + 1) % 3;
        double t = SamplingUtil.randomDouble(); // interpolation along the edge

        double px = (1.0 - t) * vx[edge] + t * vx[next];
        double pz = (1.0 - t) * vz[edge] + t * vz[next];

        int finalX = centerX + (int) Math.round(px);
        int finalZ = centerZ + (int) Math.round(pz);
        return new int[]{finalX, finalZ};
    }

    @Override
    public String getStrategyName() {
        return "triangle";
    }

    @Override
    public boolean isApplicableFor(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
        return true;
    }
}
