package com.skyblockexp.ezrtp.unsafe;

/**
 * Classifies the reason a candidate teleport location was rejected during the safety check.
 *
 * <p>Used by {@link UnsafeLocationMonitor} to break down rejection events by their root cause
 * so server operators can tune configuration accordingly.
 */
public enum UnsafeLocationCause {

    /** The block below the candidate is air or non-solid — player would fall into a void. */
    VOID("Void"),

    /** The candidate or surrounding block contains lava (destination is lava, or lava above in Nether). */
    LAVA("Lava"),

    /** The destination block itself is a liquid (water). */
    LIQUID("Liquid"),

    /** A liquid block directly above would trap the player on the surface (water above in Overworld). */
    LIQUID_SURFACE("Liquid Surface"),

    /** The block below is in the configured {@code unsafe-blocks} list. */
    UNSAFE_BLOCK("Unsafe Block"),

    /** The Y coordinate is outside the configured range, or the location is outside the world border. */
    OUT_OF_BOUNDS("Out of Bounds"),

    /** The candidate location could not be generated at all (null resolved from world). */
    NULL_CANDIDATE("Null Candidate"),

    /** A catch-all for any other safety rejection not covered by the above values. */
    OTHER("Other");

    private final String displayName;

    UnsafeLocationCause(String displayName) {
        this.displayName = displayName;
    }

    /** Returns the human-readable label used in command output and bStats charts. */
    public String getDisplayName() {
        return displayName;
    }
}
