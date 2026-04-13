package com.skyblockexp.ezrtp.teleport.biome;

import org.junit.jupiter.api.Test;

import com.skyblockexp.ezrtp.teleport.search.DiamondSearchStrategy;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiamondSearchStrategyTest {

    /**
     * Every sampled point must lie within the bounding circle of radius maxRadius
     * and no closer than minRadius * (sqrt(2)/2) from the center (the inradius of
     * the octagon at the bevel vertices).
     */
    @Test
    void staysWithinRadiusBounds() {
        DiamondSearchStrategy strategy = new DiamondSearchStrategy();
        int minRadius = 50;
        int maxRadius = 60;

        // sqrt(2)/2 ≈ 0.7071 — minimum Euclidean distance at bevel vertices.
        double innerBound = minRadius * Math.sqrt(2.0) / 2.0 - 1.0; // -1 for integer rounding

        for (int i = 0; i < 500; i++) {
            int[] coords = strategy.generateCandidateCoordinates(null, 0, 0, minRadius, maxRadius,
                    Collections.emptySet(), null);
            assertNotNull(coords);
            assertEquals(2, coords.length);

            double distance = Math.hypot(coords[0], coords[1]);
            assertTrue(distance >= innerBound,
                    "Point too close to center: distance=" + distance + " < innerBound=" + innerBound);
            assertTrue(distance <= maxRadius + 1.0,
                    "Point outside outer radius: distance=" + distance);
        }
    }

    /**
     * The Chebyshev distance (L∞) of any sampled point must be at least half the
     * chosen ring radius, since the octagon's bevel vertices sit at (r/2, r/2).
     */
    @Test
    void chebyshevDistanceRespectsOctagonShape() {
        DiamondSearchStrategy strategy = new DiamondSearchStrategy();
        int radius = 100;

        for (int i = 0; i < 500; i++) {
            int[] coords = strategy.generateCandidateCoordinates(null, 0, 0, radius, radius,
                    Collections.emptySet(), null);
            int lInf = Math.max(Math.abs(coords[0]), Math.abs(coords[1]));

            assertTrue(lInf >= radius / 2 - 1,
                    "L∞ distance too small: " + lInf + " < " + (radius / 2 - 1));
            assertTrue(lInf <= radius + 1,
                    "L∞ distance too large: " + lInf + " > " + (radius + 1));
        }
    }

    @Test
    void zeroRadiusReturnsCenter() {
        DiamondSearchStrategy strategy = new DiamondSearchStrategy();
        int[] coords = strategy.generateCandidateCoordinates(null, 5, 7, 0, 0,
                Collections.emptySet(), null);
        assertNotNull(coords);
        assertEquals(5, coords[0]);
        assertEquals(7, coords[1]);
    }

    @Test
    void centeredOnNonOrigin() {
        DiamondSearchStrategy strategy = new DiamondSearchStrategy();
        int cx = 1000, cz = -500;
        int radius = 200;

        for (int i = 0; i < 100; i++) {
            int[] coords = strategy.generateCandidateCoordinates(null, cx, cz, radius, radius,
                    Collections.emptySet(), null);
            double distance = Math.hypot(coords[0] - cx, coords[1] - cz);
            assertTrue(distance <= radius + 1.0,
                    "Point outside outer radius when center is non-origin");
        }
    }
}
