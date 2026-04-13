package com.skyblockexp.ezrtp.config.performance;

import java.util.List;

public final class PercentilesSettings {
    private static final List<Integer> DEFAULT_BUCKETS = List.of(50, 75, 90, 95, 99);

    private final boolean track;
    private final List<Integer> buckets;

    public PercentilesSettings(boolean track, List<Integer> buckets) {
        this.track = track;
        this.buckets =
                (buckets != null && !buckets.isEmpty()) ? List.copyOf(buckets) : DEFAULT_BUCKETS;
    }

    public boolean isTrack() {
        return track;
    }

    public List<Integer> getBuckets() {
        return buckets;
    }
}
