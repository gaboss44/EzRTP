package com.skyblockexp.ezrtp.teleport.biome;

import org.junit.jupiter.api.Test;

import com.skyblockexp.ezrtp.teleport.search.CircularSearchStrategy;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CircularSearchStrategyTest {

    @Test
    void staysWithinConfiguredRadius() {
        CircularSearchStrategy strategy = new CircularSearchStrategy();
        int minRadius = 100;
        int maxRadius = 200;

        for (int i = 0; i < 200; i++) {
            int[] coords = strategy.generateCandidateCoordinates(null, 0, 0, minRadius, maxRadius,
                Collections.emptySet(), null);
            double distance = Math.hypot(coords[0], coords[1]);
            assertTrue(distance >= minRadius - 1 && distance <= maxRadius + 1,
                "Generated point should stay within configured radius range");
        }
    }
}
