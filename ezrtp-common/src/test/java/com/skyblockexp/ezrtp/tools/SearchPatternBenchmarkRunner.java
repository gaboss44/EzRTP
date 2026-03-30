package com.skyblockexp.ezrtp.tools;

import com.skyblockexp.ezrtp.config.SearchPattern;
import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import com.skyblockexp.ezrtp.teleport.search.BiomeSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.CircularSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.DiamondSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.SquareSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.TriangleSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.UniformSearchStrategy;

import java.util.Collections;
import java.util.Locale;

/**
 * Lightweight benchmark harness for search-pattern candidate generation.
 *
 * <p>This class is intended for CI usage and prints machine-parseable output.</p>
 */
public final class SearchPatternBenchmarkRunner {

    private static final int DEFAULT_WARMUP_ITERATIONS = 250_000;
    private static final int DEFAULT_MEASURE_ITERATIONS = 2_000_000;

    private SearchPatternBenchmarkRunner() {
    }

    public static void main(String[] args) {
        String software = argOrDefault(args, 0, "bukkit");
        SearchPattern pattern = SearchPattern.fromConfig(argOrDefault(args, 1, "random"), SearchPattern.RANDOM);
        int warmupIterations = parsePositiveInt(argOrDefault(args, 2, String.valueOf(DEFAULT_WARMUP_ITERATIONS)),
                DEFAULT_WARMUP_ITERATIONS);
        int measureIterations = parsePositiveInt(argOrDefault(args, 3, String.valueOf(DEFAULT_MEASURE_ITERATIONS)),
                DEFAULT_MEASURE_ITERATIONS);

        BiomeSearchStrategy strategy = strategyFor(pattern);
        RareBiomeRegistry registry = null;
        long checksum = 0L;

        for (int i = 0; i < warmupIterations; i++) {
            int[] candidate = strategy.generateCandidateCoordinates(null, 0, 0, 256, 4096, Collections.emptySet(),
                    registry);
            checksum += candidate[0];
            checksum += candidate[1];
        }

        long startedAtNs = System.nanoTime();
        for (int i = 0; i < measureIterations; i++) {
            int[] candidate = strategy.generateCandidateCoordinates(null, 0, 0, 256, 4096, Collections.emptySet(),
                    registry);
            checksum += candidate[0];
            checksum += candidate[1];
        }
        long elapsedNs = System.nanoTime() - startedAtNs;

        double opsPerSecond = measureIterations * 1_000_000_000.0 / Math.max(1L, elapsedNs);
        double avgNs = elapsedNs / (double) measureIterations;

        System.out.printf(Locale.ROOT,
                "software=%s pattern=%s strategy=%s warmup=%d iterations=%d elapsed_ns=%d avg_ns=%.2f ops_per_sec=%.2f checksum=%d%n",
                software,
                pattern.getConfigKey(),
                strategy.getStrategyName(),
                warmupIterations,
                measureIterations,
                elapsedNs,
                avgNs,
                opsPerSecond,
                checksum);
    }

    private static BiomeSearchStrategy strategyFor(SearchPattern pattern) {
        return switch (pattern) {
            case RANDOM -> new UniformSearchStrategy();
            case CIRCLE -> new CircularSearchStrategy();
            case TRIANGLE -> new TriangleSearchStrategy();
            case DIAMOND -> new DiamondSearchStrategy();
            case SQUARE -> new SquareSearchStrategy();
        };
    }

    private static String argOrDefault(String[] args, int index, String fallback) {
        if (args == null || index < 0 || index >= args.length) {
            return fallback;
        }
        String value = args[index];
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int parsePositiveInt(String raw, int fallback) {
        try {
            int parsed = Integer.parseInt(raw);
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
