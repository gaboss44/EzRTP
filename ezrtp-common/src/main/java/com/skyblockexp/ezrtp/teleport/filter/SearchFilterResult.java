package com.skyblockexp.ezrtp.teleport.filter;

/**
 * Result of running a {@link SearchFilter}.
 */
public record SearchFilterResult(boolean passed, RejectionReason rejectionReason) {

    public static SearchFilterResult pass() {
        return new SearchFilterResult(true, RejectionReason.NONE);
    }

    public static SearchFilterResult rejected(RejectionReason reason) {
        return new SearchFilterResult(false, reason != null ? reason : RejectionReason.NONE);
    }

    public enum RejectionReason {
        NONE,
        UNSAFE_OR_NULL_CANDIDATE,
        BIOME_FILTERED,
        PROTECTED_CLAIM
    }
}
