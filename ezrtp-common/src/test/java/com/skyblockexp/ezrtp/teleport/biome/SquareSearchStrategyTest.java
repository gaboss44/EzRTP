package com.skyblockexp.ezrtp.teleport.biome;

import org.junit.jupiter.api.Test;

import com.skyblockexp.ezrtp.teleport.search.SquareSearchStrategy;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SquareSearchStrategyTest {

    @Test
    void walksSquarePerimeterWithinBounds() {
        SquareSearchStrategy strategy = new SquareSearchStrategy();
        int minRadius = 50;
        int maxRadius = 60;

        for (int i = 0; i < 400; i++) {
            int[] coords = strategy.generateCandidateCoordinates(null, 0, 0, minRadius, maxRadius,
                Collections.emptySet(), null);
            int absX = Math.abs(coords[0]);
            int absZ = Math.abs(coords[1]);
            int dominant = Math.max(absX, absZ);

            assertTrue(dominant >= minRadius && dominant <= maxRadius,
                "Square search should respect configured radii");
            assertTrue(absX == dominant || absZ == dominant,
                "One axis must stay locked to the current ring radius");
        }
    }
}
