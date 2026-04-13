package com.skyblockexp.ezrtp.unsafe;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe storage for unsafe location rejection counts.
 *
 * <p>Maintains two sets of counters:
 * <ul>
 *   <li><b>All-time</b> — never reset; used for bStats charts and cumulative queries.</li>
 *   <li><b>Windowed</b> — reset at each export interval; used for rate calculations and
 *       the periodic JSON export.</li>
 * </ul>
 *
 * <p>Also tracks the total number of RTP attempts at window-start so a meaningful unsafe
 * rate can be computed for the export period.
 */
public final class UnsafeLocationStatistics {

    private static final int CAUSE_COUNT = UnsafeLocationCause.values().length;

    /** All-time per-cause rejection counters (index == cause.ordinal()). */
    private final AtomicLong[] allTimeCounts;

    /** Windowed per-cause rejection counters — reset on each {@link #snapshotWindow(long, boolean)} call. */
    private final AtomicLong[] windowCounts;

    /** Wallclock time when the current window started. */
    private final AtomicReference<Instant> windowStart = new AtomicReference<>(Instant.now());

    /**
     * Total RTP attempts (from {@link com.skyblockexp.ezrtp.statistics.RtpStatistics}) at the
     * start of the current window. Used to compute per-window attempt delta at export time.
     */
    private volatile long windowStartAttemptCount = 0L;

    public UnsafeLocationStatistics() {
        allTimeCounts = new AtomicLong[CAUSE_COUNT];
        windowCounts = new AtomicLong[CAUSE_COUNT];
        for (int i = 0; i < CAUSE_COUNT; i++) {
            allTimeCounts[i] = new AtomicLong(0);
            windowCounts[i] = new AtomicLong(0);
        }
    }

    /**
     * Records an unsafe location rejection.
     *
     * @param cause the reason the location was rejected
     */
    public void recordRejection(UnsafeLocationCause cause) {
        if (cause == null) {
            cause = UnsafeLocationCause.OTHER;
        }
        int idx = cause.ordinal();
        allTimeCounts[idx].incrementAndGet();
        windowCounts[idx].incrementAndGet();
    }

    /**
     * Returns a snapshot of the current window and optionally resets it.
     *
     * @param currentTotalAttempts the current all-time RTP attempt count from
     *                             {@link com.skyblockexp.ezrtp.statistics.RtpStatistics#getTotalAttempts()};
     *                             used to compute the window attempt delta
     * @param reset                if {@code true}, zeroes window counters and advances the window
     *                             start to now, storing {@code currentTotalAttempts} as the new
     *                             baseline
     * @return an immutable {@link WindowSnapshot} of the window values before the reset
     */
    public WindowSnapshot snapshotWindow(long currentTotalAttempts, boolean reset) {
        Instant start = windowStart.get();
        Instant end = Instant.now();

        Map<UnsafeLocationCause, Long> counts = new EnumMap<>(UnsafeLocationCause.class);
        for (UnsafeLocationCause cause : UnsafeLocationCause.values()) {
            counts.put(cause, windowCounts[cause.ordinal()].get());
        }

        long windowAttempts = Math.max(0, currentTotalAttempts - windowStartAttemptCount);

        if (reset) {
            for (AtomicLong counter : windowCounts) {
                counter.set(0);
            }
            windowStart.set(end);
            windowStartAttemptCount = currentTotalAttempts;
        }

        return new WindowSnapshot(counts, windowAttempts, start, end);
    }

    /**
     * Returns an unmodifiable copy of the all-time per-cause counts.
     * Suitable for bStats chart queries.
     */
    public Map<UnsafeLocationCause, Long> getAllTimeCounts() {
        Map<UnsafeLocationCause, Long> result = new EnumMap<>(UnsafeLocationCause.class);
        for (UnsafeLocationCause cause : UnsafeLocationCause.values()) {
            result.put(cause, allTimeCounts[cause.ordinal()].get());
        }
        return result;
    }

    /**
     * Returns the all-time total unsafe rejections across all causes.
     */
    public long getAllTimeTotal() {
        long sum = 0;
        for (AtomicLong counter : allTimeCounts) {
            sum += counter.get();
        }
        return sum;
    }

    /**
     * Immutable snapshot of a single export window.
     *
     * @param counts         per-cause rejection counts for the window
     * @param windowAttempts number of RTP attempts that occurred during this window
     * @param windowStart    instant when this window began
     * @param windowEnd      instant when this snapshot was taken
     */
    public record WindowSnapshot(
            Map<UnsafeLocationCause, Long> counts,
            long windowAttempts,
            Instant windowStart,
            Instant windowEnd) {

        /** Total unsafe rejections in this window (sum of all cause counts). */
        public long totalUnsafe() {
            long sum = 0;
            for (long v : counts.values()) {
                sum += v;
            }
            return sum;
        }

        /**
         * Unsafe rejection rate for this window: {@code totalUnsafe / windowAttempts}.
         * Returns {@code 0.0} when no RTP attempts occurred during the window.
         */
        public double unsafeRate() {
            return windowAttempts > 0 ? (double) totalUnsafe() / windowAttempts : 0.0;
        }
    }
}
