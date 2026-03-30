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

        double r = radius;
        double half = r * 0.5;
        double[][] verts = new double[][]{
            {0.0, -r},            // top
            {half, -half},        // upper-right bevel
            {r, 0.0},             // right
            {half, half},         // lower-right bevel
            {0.0, r},             // bottom
            {-half, half},        // lower-left bevel
            {-r, 0.0},            // left
            {-half, -half}        // upper-left bevel
        };

        // Compute approximate integer-perimeter lengths for each edge and sample a single
        // index without allocating the full point list to avoid heavy CPU/memory usage.
        int[] edgeSteps = new int[verts.length];
        int totalSteps = 0;
        for (int e = 0; e < verts.length; e++) {
            int next = (e + 1) % verts.length;
            double dx = verts[next][0] - verts[e][0];
            double dz = verts[next][1] - verts[e][1];
            int steps = Math.max(1, (int) Math.round(Math.hypot(dx, dz)));
            edgeSteps[e] = steps;
            totalSteps = Math.addExact(totalSteps, steps);
        }

        if (totalSteps <= 0) {
            return new int[]{centerX, centerZ};
        }

        int idx = ThreadLocalRandom.current().nextInt(totalSteps);
        int accum = 0;
        int chosenEdge = 0;
        int chosenStep = 0;
        for (int e = 0; e < edgeSteps.length; e++) {
            int es = edgeSteps[e];
            if (idx < accum + es) {
                chosenEdge = e;
                chosenStep = idx - accum;
                break;
            }
            accum += es;
        }

        int nextEdge = (chosenEdge + 1) % verts.length;
        double x1 = verts[chosenEdge][0];
        double z1 = verts[chosenEdge][1];
        double x2 = verts[nextEdge][0];
        double z2 = verts[nextEdge][1];
        double dx = x2 - x1;
        double dz = z2 - z1;
        int steps = edgeSteps[chosenEdge];
        double t = chosenStep / (double) steps;
        int px = centerX + (int) Math.round(x1 + dx * t);
        int pz = centerZ + (int) Math.round(z1 + dz * t);
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
