package com.skyblockexp.ezrtp.tools;

import com.skyblockexp.ezrtp.teleport.Measurements;

/**
 * Simple microbenchmark simulator that models candidate generation, chunk-load
 * requests and block accesses. This is intentionally offline and does not
 * require a running server; it uses the Measurements counters for quick
 * before/after comparisons.
 */
public final class MicrobenchSimulator {
    public static void main(String[] args) {
        int trials = 10000;
        int avgVerticalChecks = 8;
        double snapshotHitRate = 0.25; // fraction of candidates handled by snapshot fast-path

        if (args.length > 0) trials = Integer.parseInt(args[0]);
        if (args.length > 1) avgVerticalChecks = Integer.parseInt(args[1]);
        if (args.length > 2) snapshotHitRate = Double.parseDouble(args[2]);

        System.out.println("Microbench Simulator\ntrials=" + trials + " avgChecks=" + avgVerticalChecks + " snapshotRate=" + snapshotHitRate);

        // Legacy run (no snapshot): every cache generates a chunk load and vertical scans
        Measurements.reset();
        for (int i = 0; i < trials; i++) {
            Measurements.incCandidateGeneration();
            Measurements.incChunkLoadRequest();
            for (int j = 0; j < avgVerticalChecks; j++) Measurements.incBlockAccess();
        }
        long legacyCandidates = Measurements.getCandidateGenerations();
        long legacyChunkLoads = Measurements.getChunkLoadRequests();
        long legacyBlockAccesses = Measurements.getBlockAccesses();

        // Snapshot-enabled run (some candidates resolved via snapshot)
        Measurements.reset();
        java.util.Random rnd = new java.util.Random(42);
        for (int i = 0; i < trials; i++) {
            Measurements.incCandidateGeneration();
            if (rnd.nextDouble() < snapshotHitRate) {
                // snapshot resolves without chunk load and with fewer block accesses
                for (int j = 0; j < Math.max(1, avgVerticalChecks / 3); j++) Measurements.incBlockAccess();
            } else {
                Measurements.incChunkLoadRequest();
                for (int j = 0; j < avgVerticalChecks; j++) Measurements.incBlockAccess();
            }
        }
        long snapCandidates = Measurements.getCandidateGenerations();
        long snapChunkLoads = Measurements.getChunkLoadRequests();
        long snapBlockAccesses = Measurements.getBlockAccesses();

        System.out.println("\nResults:");
        System.out.println("Legacy -> candidates=" + legacyCandidates + " chunks=" + legacyChunkLoads + " blocks=" + legacyBlockAccesses);
        System.out.println("Snapshot-> candidates=" + snapCandidates + " chunks=" + snapChunkLoads + " blocks=" + snapBlockAccesses);
        System.out.println("Chunk loads saved: " + (legacyChunkLoads - snapChunkLoads));
        System.out.println("Block accesses saved: " + (legacyBlockAccesses - snapBlockAccesses));
    }
}
