package com.skyblockexp.ezrtp.teleport.search;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Small sampling utilities to consolidate random sampling logic.
 */
public final class SamplingUtil {

    private SamplingUtil() {}

    public static int randomIntInclusive(int min, int max) {
        if (min >= max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static double randomAngle() {
        return ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
    }

    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }
}
