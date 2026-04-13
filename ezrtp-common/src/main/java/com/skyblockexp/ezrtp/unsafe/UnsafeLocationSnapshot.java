package com.skyblockexp.ezrtp.unsafe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of an unsafe location monitoring export window.
 *
 * <p>Carries all data required to produce console warnings, the periodic JSON export,
 * and the {@code /rtp unsafe-stats} command output.
 */
public final class UnsafeLocationSnapshot {

    private static final double RECOMMENDATION_THRESHOLD = 0.30;

    private final String timestamp;
    private final int periodMinutes;
    private final long totalAttempts;
    private final long unsafeAttempts;
    private final double unsafeRate;
    private final Map<UnsafeLocationCause, Long> causeCounts;
    private final List<String> recommendations;

    public UnsafeLocationSnapshot(
            String timestamp,
            int periodMinutes,
            long totalAttempts,
            long unsafeAttempts,
            double unsafeRate,
            Map<UnsafeLocationCause, Long> causeCounts) {
        this.timestamp = timestamp;
        this.periodMinutes = periodMinutes;
        this.totalAttempts = totalAttempts;
        this.unsafeAttempts = unsafeAttempts;
        this.unsafeRate = unsafeRate;
        this.causeCounts = Collections.unmodifiableMap(new EnumMap<>(causeCounts));
        this.recommendations = Collections.unmodifiableList(computeRecommendations(unsafeAttempts, causeCounts));
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getPeriodMinutes() {
        return periodMinutes;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public long getUnsafeAttempts() {
        return unsafeAttempts;
    }

    public double getUnsafeRate() {
        return unsafeRate;
    }

    public Map<UnsafeLocationCause, Long> getCauseCounts() {
        return causeCounts;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    /**
     * Serialises this snapshot to a JSON string matching the documented export format.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"timestamp\": \"").append(timestamp).append("\",\n");
        sb.append("  \"period_minutes\": ").append(periodMinutes).append(",\n");
        sb.append("  \"rtp_operations\": {\n");
        sb.append("    \"total_attempts\": ").append(totalAttempts).append(",\n");
        sb.append("    \"unsafe_attempts\": ").append(unsafeAttempts).append(",\n");
        sb.append(String.format("    \"unsafe_rate\": %.3f%n", unsafeRate));
        sb.append("  },\n");
        sb.append("  \"unsafe_causes\": {\n");
        UnsafeLocationCause[] causes = UnsafeLocationCause.values();
        for (int i = 0; i < causes.length; i++) {
            long count = causeCounts.getOrDefault(causes[i], 0L);
            sb.append("    \"")
                    .append(causes[i].name().toLowerCase())
                    .append("\": ")
                    .append(count);
            if (i < causes.length - 1) {
                sb.append(',');
            }
            sb.append('\n');
        }
        sb.append("  },\n");
        sb.append("  \"recommendations\": [\n");
        for (int i = 0; i < recommendations.size(); i++) {
            sb.append("    \"").append(escapeJson(recommendations.get(i))).append("\"");
            if (i < recommendations.size() - 1) {
                sb.append(',');
            }
            sb.append('\n');
        }
        sb.append("  ]\n");
        sb.append("}");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static List<String> computeRecommendations(
            long totalUnsafe, Map<UnsafeLocationCause, Long> counts) {
        if (totalUnsafe == 0) {
            return List.of();
        }
        List<String> recs = new ArrayList<>();
        for (Map.Entry<UnsafeLocationCause, Long> entry : counts.entrySet()) {
            if (entry.getValue() == 0) {
                continue;
            }
            double share = (double) entry.getValue() / totalUnsafe;
            if (share < RECOMMENDATION_THRESHOLD) {
                continue;
            }
            switch (entry.getKey()) {
                case VOID -> recs.add(
                        "High void rejections: consider raising min Y limit.");
                case LAVA -> recs.add(
                        "High lava rejections: consider adjusting Y range or expanding radius.");
                case LIQUID, LIQUID_SURFACE -> recs.add(
                        "High liquid rejections: consider restricting radius away from ocean biomes.");
                case UNSAFE_BLOCK -> recs.add(
                        "Unsafe block rejections high: review unsafe-blocks list in rtp.yml.");
                case OUT_OF_BOUNDS -> recs.add(
                        "Many out-of-bounds rejections: consider expanding teleport radius or adjusting Y limits.");
                case NULL_CANDIDATE -> recs.add(
                        "Frequent null candidate rejections: check world height and radius configuration.");
                default -> {
                    // no recommendation for OTHER
                }
            }
        }
        return recs;
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
