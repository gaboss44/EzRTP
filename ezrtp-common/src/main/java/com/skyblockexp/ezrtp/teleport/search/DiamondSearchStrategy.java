package com.skyblockexp.ezrtp.teleport.search;

import org.bukkit.World;
import org.bukkit.block.Biome;

import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;

import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;

/**
 * Generates coordinates by selecting a random point on the perimeter of a diamond (rotated square)
 * centered on the configured center point. The diamond is defined by the requested radius.
 */
public final class DiamondSearchStrategy implements BiomeSearchStrategy {

    // Unit-space vertices of the 8-vertex chamfered diamond (octagonal lozenge).
    // All coordinates are expressed as multiples of the radius so they can be scaled
    // at call time without any per-call array allocation.
    // Order: top, upper-right bevel, right, lower-right bevel,
    //        bottom, lower-left bevel, left, upper-left bevel.
    private static final double[] UNIT_VERTS_X = { 0.0,  0.5,  1.0,  0.5,  0.0, -0.5, -1.0, -0.5};
    private static final double[] UNIT_VERTS_Z = {-1.0, -0.5,  0.0,  0.5,  1.0,  0.5,  0.0, -0.5};

    // All 8 edges are identical in length in unit space: hypot(0.5, 0.5) = sqrt(0.5).
    // Pre-computing avoids calling Math.hypot inside the hot path.
    private static final double UNIT_EDGE_LENGTH = Math.hypot(0.5, 0.5);

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

        int radius = effectiveMin == effectiveMax ? effectiveMin : SamplingUtil.randomIntInclusive(effectiveMin, effectiveMax);
        // Build an 8-vertex chamfered diamond (octagonal lozenge) to approximate the
        // Minecraft diamond silhouette. Vertices are placed at: top, upper-right-bevel,
        // right, lower-right-bevel, bottom, lower-left-bevel, left, upper-left-bevel.
        if (radius <= 0) {
            return new int[]{centerX, centerZ};
        }

        // All 8 edges have the same integer step count because the octagon is symmetric.
        // Using division/modulo instead of a loop avoids allocating an int[] per call.
        int edgeSteps = Math.max(1, (int) Math.round(radius * UNIT_EDGE_LENGTH));
        int totalSteps = 8 * edgeSteps;

        int idx = ThreadLocalRandom.current().nextInt(totalSteps);
        int chosenEdge = idx / edgeSteps;
        int chosenStep = idx % edgeSteps;

        int nextEdge = (chosenEdge + 1) % 8;
        double x1 = UNIT_VERTS_X[chosenEdge];
        double z1 = UNIT_VERTS_Z[chosenEdge];
        double dx = UNIT_VERTS_X[nextEdge] - x1;
        double dz = UNIT_VERTS_Z[nextEdge] - z1;
        double t = chosenStep / (double) edgeSteps;
        int px = centerX + (int) Math.round((x1 + dx * t) * radius);
        int pz = centerZ + (int) Math.round((z1 + dz * t) * radius);
        return new int[]{px, pz};
    }

    @Override
    public String getStrategyName() {
        return "diamond";
    }

    @Override
    public boolean isApplicableFor(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
        return true;
    }
}
