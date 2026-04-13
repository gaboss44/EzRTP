package com.skyblockexp.ezrtp.config.teleport;

import com.skyblockexp.ezrtp.config.EzRtpConfiguration;

/**
 * Holds parsed RTP cooldown and per-player usage limit settings for a world/group combination.
 *
 * <p>Instances are immutable value objects produced by
 * {@link EzRtpConfiguration#getLimitSettings(String, String)}.
 */
public final class RtpLimitSettings {

    private final int cooldownSeconds;
    private final int dailyLimit;
    private final int weeklyLimit;
    private final Double cost;
    private final boolean disableDailyLimit;

    /**
     * Creates a limit settings object with daily-limit enforcement enabled.
     *
     * @param cooldownSeconds seconds a player must wait between teleports
     * @param dailyLimit      maximum teleports per day (0 = unlimited)
     * @param weeklyLimit     maximum teleports per week (0 = unlimited)
     * @param cost            optional economy cost per teleport ({@code null} = use world default)
     */
    public RtpLimitSettings(int cooldownSeconds, int dailyLimit, int weeklyLimit, Double cost) {
        this(cooldownSeconds, dailyLimit, weeklyLimit, cost, false);
    }

    /**
     * Creates a fully specified limit settings object.
     *
     * @param cooldownSeconds   seconds a player must wait between teleports
     * @param dailyLimit        maximum teleports per day (0 = unlimited)
     * @param weeklyLimit       maximum teleports per week (0 = unlimited)
     * @param cost              optional economy cost per teleport ({@code null} = use world default)
     * @param disableDailyLimit when {@code true} the daily/weekly counters are ignored entirely
     */
    public RtpLimitSettings(
            int cooldownSeconds,
            int dailyLimit,
            int weeklyLimit,
            Double cost,
            boolean disableDailyLimit) {
        this.cooldownSeconds = cooldownSeconds;
        this.dailyLimit = dailyLimit;
        this.weeklyLimit = weeklyLimit;
        this.cost = cost;
        this.disableDailyLimit = disableDailyLimit;
    }

    /**
     * Returns the number of seconds a player must wait between random teleports.
     *
     * @return cooldown in seconds; 0 means no cooldown
     */
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    /**
     * Returns the maximum number of random teleports allowed per day.
     *
     * @return daily cap; 0 means unlimited
     */
    public int getDailyLimit() {
        return dailyLimit;
    }

    /**
     * Returns the maximum number of random teleports allowed per week.
     *
     * @return weekly cap; 0 means unlimited
     */
    public int getWeeklyLimit() {
        return weeklyLimit;
    }

    /**
     * Returns the optional per-teleport economy cost override for this limit group.
     *
     * @return cost, or {@code null} if no override is set for this group
     */
    public Double getCost() {
        return cost;
    }

    /**
     * Returns {@code true} if the daily/weekly usage counters should be ignored for players in
     * this limit group.
     *
     * @return {@code true} to skip daily/weekly enforcement
     */
    public boolean isDisableDailyLimit() {
        return disableDailyLimit;
    }
}
