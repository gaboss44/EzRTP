package com.skyblockexp.ezrtp.config.teleport;

/**
 * Supported spatial search patterns for candidate coordinate generation.
 */
public enum SearchPattern {
    RANDOM("random"),
    CIRCLE("circle"),
    TRIANGLE("triangle"),
    DIAMOND("diamond"),
    SQUARE("square");

    private final String configKey;

    SearchPattern(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    public static SearchPattern fromConfig(String rawValue, SearchPattern fallback) {
        if (rawValue == null || rawValue.isBlank()) {
            return fallback != null ? fallback : RANDOM;
        }
        for (SearchPattern pattern : values()) {
            if (pattern.name().equalsIgnoreCase(rawValue) || pattern.configKey.equalsIgnoreCase(rawValue)) {
                return pattern;
            }
        }
        return fallback != null ? fallback : RANDOM;
    }
}
