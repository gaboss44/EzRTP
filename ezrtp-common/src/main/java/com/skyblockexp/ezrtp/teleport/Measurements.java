package com.skyblockexp.ezrtp.teleport;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Lightweight runtime counters to assist microbenchmarks.
 * Counters are global and thread-safe.
 */
public final class Measurements {
    private static final AtomicLong blockAccesses = new AtomicLong(0);
    private static final AtomicLong chunkLoadRequests = new AtomicLong(0);
    private static final AtomicLong candidateGenerations = new AtomicLong(0);

    private Measurements() {}

    public static void reset() {
        blockAccesses.set(0);
        chunkLoadRequests.set(0);
        candidateGenerations.set(0);
    }

    public static void incBlockAccess() { blockAccesses.incrementAndGet(); }
    public static void incChunkLoadRequest() { chunkLoadRequests.incrementAndGet(); }
    public static void incCandidateGeneration() { candidateGenerations.incrementAndGet(); }

    public static long getBlockAccesses() { return blockAccesses.get(); }
    public static long getChunkLoadRequests() { return chunkLoadRequests.get(); }
    public static long getCandidateGenerations() { return candidateGenerations.get(); }
}
