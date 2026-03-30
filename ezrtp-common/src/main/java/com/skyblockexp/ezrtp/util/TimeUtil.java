package com.skyblockexp.ezrtp.util;

/**
 * Utility class for time-related formatting and operations.
 */
public final class TimeUtil {

    private TimeUtil() {
        // Utility class
    }

    /**
     * Formats cooldown time in a human-readable format.
     * @param seconds the time in seconds
     * @return formatted time string (e.g., "30s", "5m 30s", "2h 15m")
     */
    public static String formatCooldownTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m " + (seconds % 60) + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "h " + minutes + "m";
        }
    }
}
