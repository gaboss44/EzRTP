package com.skyblockexp.ezrtp.statistics;

import org.bukkit.block.Biome;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.Deque;

/**
 * Tracks statistics for random teleport operations, including overall metrics
 * and per-biome performance data. Thread-safe for concurrent access.
 */
public final class RtpStatistics {

    private static final int MAX_RECENT_RESULTS = 1000;

    // Overall statistics
    private final AtomicInteger totalAttempts = new AtomicInteger(0);
    private final AtomicInteger totalSuccesses = new AtomicInteger(0);
    private final AtomicInteger totalFailures = new AtomicInteger(0);
    private final AtomicInteger totalBiomeFailures = new AtomicInteger(0);
    private final AtomicInteger totalCacheHits = new AtomicInteger(0);
    private final AtomicInteger totalCacheMisses = new AtomicInteger(0);
    private final AtomicLong totalTeleportTimeMs = new AtomicLong(0);

    // Recent results for moving average success rate
    private final Deque<Boolean> recentResults = new ConcurrentLinkedDeque<>();

    // Per-biome statistics
    private final Map<Biome, BiomeStats> biomeStats = new ConcurrentHashMap<>();

    // Failure cause tracking

    // -------------------------------------------------------------------------
    // Timing ring buffer for percentile tracking (last 2048 successful RTPs)
    // -------------------------------------------------------------------------
    private static final int TIMING_BUFFER_SIZE = 2048;
    private final long[] timingSamples = new long[TIMING_BUFFER_SIZE];
    private int timingWriteIndex = 0;
    private int timingFilled = 0;
    private final AtomicLong minTimeMs = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxTimeMs = new AtomicLong(0L);

    // Chunk load counters
    private final AtomicLong totalChunkLoads = new AtomicLong(0);
    private final AtomicLong asyncChunkLoads = new AtomicLong(0);
    private final AtomicLong syncChunkLoads = new AtomicLong(0);

    // Biome rejection counters
    private final AtomicLong totalBiomeRejections = new AtomicLong(0);
    private final AtomicInteger maxBiomeRejectionsPerRtp = new AtomicInteger(0);
    private final AtomicInteger biomeFilterTimeouts = new AtomicInteger(0);
    private final AtomicInteger failuresSafety = new AtomicInteger(0);
    private final AtomicInteger failuresProtection = new AtomicInteger(0);
    private final AtomicInteger failuresBiome = new AtomicInteger(0);
    private final AtomicInteger failuresTimeout = new AtomicInteger(0);
    private final AtomicInteger failuresEconomy = new AtomicInteger(0);
    private final AtomicInteger failuresPlayerOfflineOrCancelled = new AtomicInteger(0);
    private final AtomicInteger failuresTeleportApi = new AtomicInteger(0);
    private final AtomicInteger failuresGenericSearchError = new AtomicInteger(0);
    
    // Search strategy tracking
    private final AtomicInteger weightedSearchUses = new AtomicInteger(0);
    private final AtomicInteger uniformSearchUses = new AtomicInteger(0);
    private final AtomicInteger chunkLoadQueueHits = new AtomicInteger(0);
    
    /**
     * Records use of weighted search strategy.
     */
    public void recordWeightedSearchUse() {
        weightedSearchUses.incrementAndGet();
    }
    
    /**
     * Records use of uniform search strategy.
     */
    public void recordUniformSearchUse() {
        uniformSearchUses.incrementAndGet();
    }
    
    /**
     * Records use of chunk load queue.
     */
    public void recordChunkLoadQueueUse() {
        chunkLoadQueueHits.incrementAndGet();
    }
    
    /**
     * Returns the number of times weighted search was used.
     */
    public int getWeightedSearchUses() {
        return weightedSearchUses.get();
    }
    
    /**
     * Returns the number of times uniform search was used.
     */
    public int getUniformSearchUses() {
        return uniformSearchUses.get();
    }
    
    /**
     * Returns the number of times chunk load queue was used.
     */
    public int getChunkLoadQueueHits() {
        return chunkLoadQueueHits.get();
    }
    
    /**
     * Records an RTP attempt.
     * A cache miss means cache lookup was performed but no cached location was used.
     */
    public void recordAttempt(boolean success, long durationMs, Biome biome, boolean cacheHit, boolean cacheChecked) {
        totalAttempts.incrementAndGet();

        if (success) {
            totalSuccesses.incrementAndGet();
            totalTeleportTimeMs.addAndGet(durationMs);
            recordTimingSample(durationMs);

            if (biome != null) {
                BiomeStats stats = getBiomeStats(biome);
                stats.recordSuccess(durationMs);
                // Record cache hit for this specific biome if applicable
                if (cacheHit) {
                    stats.recordCacheHit();
                }
            }
        } else {
            totalFailures.incrementAndGet();
        }
        
        if (cacheHit) {
            totalCacheHits.incrementAndGet();
        } else if (cacheChecked) {
            totalCacheMisses.incrementAndGet();
        }
        
        // Update recent results for moving average
        recentResults.addLast(success);
        if (recentResults.size() > MAX_RECENT_RESULTS) {
            recentResults.removeFirst();
        }
    }

    /**
     * Records an RTP attempt when cache lookup status is unknown.
     * Misses are not inferred in this overload.
     */
    public void recordAttempt(boolean success, long durationMs, Biome biome, boolean cacheHit) {
        recordAttempt(success, durationMs, biome, cacheHit, cacheHit);
    }
    
    /**
     * Records a biome-related failure (only called when search times out due to biome filtering).
     */
    public void recordBiomeFailure(Biome targetBiome) {
        totalBiomeFailures.incrementAndGet();
        failuresBiome.incrementAndGet();
        if (targetBiome != null) {
            getBiomeStats(targetBiome).recordFailure();
        }
    }
    
    /**
     * Records a biome search attempt that was rejected (for bStats tracking only).
     * This doesn't count as a "failure" but tracks per-biome activity.
     */
    public void recordBiomeAttempt(Biome biome) {
        totalBiomeRejections.incrementAndGet();
        if (biome != null) {
            getBiomeStats(biome).recordAttempt();
        }
    }
    
    /**
     * Records a failure due to safety checks (unsafe blocks, etc.).
     */
    public void recordSafetyFailure() {
        failuresSafety.incrementAndGet();
    }
    
    /**
     * Records a failure due to protection (claims, regions, etc.).
     */
    public void recordProtectionFailure() {
        failuresProtection.incrementAndGet();
    }
    
    /**
     * Records a failure due to timeout (max attempts exceeded).
     */
    public void recordTimeoutFailure() {
        failuresTimeout.incrementAndGet();
    }

    /**
     * Records a failure due to economy/payment constraints.
     */
    public void recordEconomyFailure() {
        failuresEconomy.incrementAndGet();
    }

    /**
     * Records a failure because the player is offline or the teleport was cancelled.
     */
    public void recordPlayerOfflineOrCancelledFailure() {
        failuresPlayerOfflineOrCancelled.incrementAndGet();
    }

    /**
     * Records a failure returned by the Bukkit teleport API.
     */
    public void recordTeleportApiFailure() {
        failuresTeleportApi.incrementAndGet();
    }

    /**
     * Records a generic search/runtime failure where no more specific bucket applies.
     */
    public void recordGenericSearchErrorFailure() {
        failuresGenericSearchError.incrementAndGet();
    }
    
    /**
     * Gets or creates statistics for a specific biome.
     */
    private BiomeStats getBiomeStats(Biome biome) {
        return biomeStats.computeIfAbsent(biome, k -> new BiomeStats());
    }
    
    /**
     * Returns the total number of RTP attempts.
     */
    public int getTotalAttempts() {
        return totalAttempts.get();
    }
    
    /**
     * Returns the total number of successful RTPs.
     */
    public int getTotalSuccesses() {
        return totalSuccesses.get();
    }
    
    /**
     * Returns the total number of failed RTPs.
     */
    public int getTotalFailures() {
        return totalFailures.get();
    }
    
    /**
     * Returns the total number of biome-related failures.
     */
    public int getTotalBiomeFailures() {
        return totalBiomeFailures.get();
    }
    
    /**
     * Returns the total number of cache hits.
     */
    public int getTotalCacheHits() {
        return totalCacheHits.get();
    }
    
    /**
     * Returns the total number of cache misses.
     */
    public int getTotalCacheMisses() {
        return totalCacheMisses.get();
    }
    
    /**
     * Returns the total teleport time in milliseconds.
     */
    public long getTotalTeleportTimeMs() {
        return totalTeleportTimeMs.get();
    }
    
    /**
     * Returns the average teleport time in milliseconds.
     */
    public double getAverageTeleportTimeMs() {
        int successes = totalSuccesses.get();
        if (successes == 0) {
            return 0.0;
        }
        return (double) totalTeleportTimeMs.get() / successes;
    }
    
    /**
     * Returns the success rate as a percentage (0-100), based on recent attempts (last 1000).
     */
    public double getSuccessRate() {
        if (recentResults.isEmpty()) {
            return 0.0;
        }
        long successCount = recentResults.stream().filter(Boolean::booleanValue).count();
        return Math.min(100.0, (double) successCount / recentResults.size() * 100.0);
    }
    
    /**
     * Returns the cache hit rate as a percentage (0-100).
     */
    public double getCacheHitRate() {
        int total = totalCacheHits.get() + totalCacheMisses.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalCacheHits.get() / total * 100.0;
    }
    
    /**
     * Returns statistics for all biomes.
     */
    public Map<Biome, BiomeStats> getBiomeStats() {
        return new ConcurrentHashMap<>(biomeStats);
    }
    
    /**
     * Returns statistics for a specific biome, or null if no data exists.
     */
    public BiomeStats getBiomeStatsFor(Biome biome) {
        return biomeStats.get(biome);
    }
    
    /**
     * Returns failure counts by cause.
     */
    public FailureCauses getFailureCauses() {
        return new FailureCauses(
            failuresSafety.get(),
            failuresProtection.get(),
            failuresBiome.get(),
            failuresTimeout.get(),
            failuresEconomy.get(),
            failuresPlayerOfflineOrCancelled.get(),
            failuresTeleportApi.get(),
            failuresGenericSearchError.get()
        );
    }
    
    /**
     * Resets all statistics to zero.
     */
    public void reset() {
        totalAttempts.set(0);
        totalSuccesses.set(0);
        totalFailures.set(0);
        totalBiomeFailures.set(0);
        totalCacheHits.set(0);
        totalCacheMisses.set(0);
        totalTeleportTimeMs.set(0);
        biomeStats.clear();
        failuresSafety.set(0);
        failuresProtection.set(0);
        failuresBiome.set(0);
        failuresTimeout.set(0);
        failuresEconomy.set(0);
        failuresPlayerOfflineOrCancelled.set(0);
        failuresTeleportApi.set(0);
        failuresGenericSearchError.set(0);
        weightedSearchUses.set(0);
        uniformSearchUses.set(0);
        chunkLoadQueueHits.set(0);
        recentResults.clear();
        synchronized (this) {
            timingWriteIndex = 0;
            timingFilled = 0;
            Arrays.fill(timingSamples, 0L);
        }
        minTimeMs.set(Long.MAX_VALUE);
        maxTimeMs.set(0L);
        totalChunkLoads.set(0);
        asyncChunkLoads.set(0);
        syncChunkLoads.set(0);
        totalBiomeRejections.set(0);
        maxBiomeRejectionsPerRtp.set(0);
        biomeFilterTimeouts.set(0);
    }

    // -------------------------------------------------------------------------
    // Timing percentile methods
    // -------------------------------------------------------------------------

    private synchronized void recordTimingSample(long ms) {
        timingSamples[timingWriteIndex % TIMING_BUFFER_SIZE] = ms;
        timingWriteIndex++;
        if (timingFilled < TIMING_BUFFER_SIZE) {
            timingFilled++;
        }
        // update all-time min / max with CAS loops
        long oldMin = minTimeMs.get();
        while (ms < oldMin && !minTimeMs.compareAndSet(oldMin, ms)) {
            oldMin = minTimeMs.get();
        }
        long oldMax = maxTimeMs.get();
        while (ms > oldMax && !maxTimeMs.compareAndSet(oldMax, ms)) {
            oldMax = maxTimeMs.get();
        }
    }

    public synchronized double getTimingPercentile(int percentile) {
        if (timingFilled == 0) {
            return 0.0;
        }
        long[] snapshot = Arrays.copyOf(timingSamples, timingFilled);
        Arrays.sort(snapshot);
        int idx = (int) Math.ceil(percentile / 100.0 * timingFilled) - 1;
        return snapshot[Math.max(0, Math.min(idx, timingFilled - 1))];
    }

    public synchronized int getSlowOperationCount(long thresholdMs) {
        if (timingFilled == 0) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < timingFilled; i++) {
            if (timingSamples[i] > thresholdMs) {
                count++;
            }
        }
        return count;
    }

    public long getMinTimeMs() {
        long v = minTimeMs.get();
        return v == Long.MAX_VALUE ? 0L : v;
    }

    public long getMaxTimeMs() {
        return maxTimeMs.get();
    }

    // -------------------------------------------------------------------------
    // Chunk load tracking
    // -------------------------------------------------------------------------

    public void recordChunkLoad(boolean isAsync) {
        totalChunkLoads.incrementAndGet();
        if (isAsync) {
            asyncChunkLoads.incrementAndGet();
        } else {
            syncChunkLoads.incrementAndGet();
        }
    }

    public long getTotalChunkLoads() {
        return totalChunkLoads.get();
    }

    public long getAsyncChunkLoads() {
        return asyncChunkLoads.get();
    }

    public long getSyncChunkLoads() {
        return syncChunkLoads.get();
    }

    // -------------------------------------------------------------------------
    // Biome rejection tracking
    // -------------------------------------------------------------------------

    public void recordBiomeRejectionCount(int count) {
        if (count <= 0) {
            return;
        }
        int old = maxBiomeRejectionsPerRtp.get();
        while (count > old && !maxBiomeRejectionsPerRtp.compareAndSet(old, count)) {
            old = maxBiomeRejectionsPerRtp.get();
        }
    }

    public void recordBiomeFilterTimeout() {
        biomeFilterTimeouts.incrementAndGet();
    }

    public long getTotalBiomeRejections() {
        return totalBiomeRejections.get();
    }

    public int getMaxBiomeRejectionsPerRtp() {
        return maxBiomeRejectionsPerRtp.get();
    }

    public int getBiomeFilterTimeouts() {
        return biomeFilterTimeouts.get();
    }
    
    /**
     * Per-biome statistics container.
     */
    public static final class BiomeStats {
        private final AtomicInteger attempts = new AtomicInteger(0);
        private final AtomicInteger successes = new AtomicInteger(0);
        private final AtomicInteger failures = new AtomicInteger(0);
        private final AtomicInteger cacheHits = new AtomicInteger(0);
        private final AtomicLong totalTimeMs = new AtomicLong(0);
        
        void recordSuccess(long durationMs) {
            attempts.incrementAndGet();
            successes.incrementAndGet();
            totalTimeMs.addAndGet(durationMs);
        }
        
        void recordFailure() {
            attempts.incrementAndGet();
            failures.incrementAndGet();
        }
        
        void recordAttempt() {
            attempts.incrementAndGet();
        }
        
        /**
         * Records a cache hit for this biome.
         * Cache hits represent successful uses of pre-cached locations.
         */
        void recordCacheHit() {
            cacheHits.incrementAndGet();
        }
        
        public int getAttempts() {
            return attempts.get();
        }
        
        public int getSuccesses() {
            return successes.get();
        }
        
        public int getFailures() {
            return failures.get();
        }
        
        /**
         * Returns the total number of cache hits for this biome.
         * This represents the number of times a pre-cached location was successfully used.
         */
        public int getCacheHits() {
            return cacheHits.get();
        }
        
        /**
         * Returns the total number of times this biome was found/located.
         * This includes both successful new searches and cache hits.
         */
        public int getTotalFinds() {
            return successes.get() + cacheHits.get();
        }
        
        public double getSuccessRate() {
            int total = attempts.get();
            if (total == 0) {
                return 0.0;
            }
            return Math.min(100.0, (double) successes.get() / total * 100.0);
        }
        
        public double getAverageTimeMs() {
            int count = successes.get();
            if (count == 0) {
                return 0.0;
            }
            return (double) totalTimeMs.get() / count;
        }
    }
    
    /**
     * Failure cause breakdown.
     */
    public record FailureCauses(
        int safety,
        int protection,
        int biome,
        int timeout,
        int economy,
        int playerOfflineOrCancelled,
        int teleportApi,
        int genericSearchError
    ) {
        public int total() {
            return safety + protection + biome + timeout + economy + playerOfflineOrCancelled + teleportApi + genericSearchError;
        }
    }
}
