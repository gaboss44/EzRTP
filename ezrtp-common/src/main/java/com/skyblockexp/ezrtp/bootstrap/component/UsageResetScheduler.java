package com.skyblockexp.ezrtp.bootstrap.component;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.storage.RtpUsageStorage;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Level;

/**
 * Schedules and executes periodic resets for RTP usage statistics.
 */
public final class UsageResetScheduler {

    private final EzRtpPlugin plugin;
    private final PlatformScheduler scheduler;
    private final RtpUsageStorage usageStorage;

    public UsageResetScheduler(
            EzRtpPlugin plugin, PlatformScheduler scheduler, RtpUsageStorage usageStorage) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.usageStorage = usageStorage;
    }

    public void schedule() {
        long ticksPerDay = 24 * 60 * 60 * 20L;
        long ticksUntilMidnight = getTicksUntilNext(0, 0);
        scheduler.scheduleRepeating(this::resetDailyUsage, ticksUntilMidnight, ticksPerDay);

        long ticksPerWeek = 7 * 24 * 60 * 60 * 20L;
        long ticksUntilSunday = getTicksUntilNextSundayMidnight();
        scheduler.scheduleRepeating(this::resetWeeklyUsage, ticksUntilSunday, ticksPerWeek);
    }

    private void resetDailyUsage() {
        try {
            usageStorage.resetUsage(null, null, "daily");
            usageStorage.save();
            plugin.getLogger().info("[EzRTP] Daily RTP usage counts reset.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[EzRTP] Failed to reset daily RTP usage counts", e);
        }
    }

    private void resetWeeklyUsage() {
        try {
            usageStorage.resetUsage(null, null, "weekly");
            usageStorage.save();
            plugin.getLogger().info("[EzRTP] Weekly RTP usage counts reset.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[EzRTP] Failed to reset weekly RTP usage counts", e);
        }
    }

    private long getTicksUntilNext(int hour, int minute) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }
        long seconds = Duration.between(now, next).getSeconds();
        return seconds * 20L;
    }

    private long getTicksUntilNextSundayMidnight() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime next = now.with(DayOfWeek.SUNDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
        if (!next.isAfter(now)) {
            next = next.plusWeeks(1);
        }
        long seconds = Duration.between(now, next).getSeconds();
        return seconds * 20L;
    }
}
