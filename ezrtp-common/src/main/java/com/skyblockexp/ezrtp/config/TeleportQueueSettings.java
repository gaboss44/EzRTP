package com.skyblockexp.ezrtp.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Represents the configuration for EzRTP's request queue which serialises teleport attempts.
 */
public final class TeleportQueueSettings {

    private final boolean enabled;
    private final int maxSize;
    private final String bypassPermission;
    private final long startDelayTicks;
    private final long intervalTicks;

    private TeleportQueueSettings(boolean enabled, int maxSize, String bypassPermission,
                                  long startDelayTicks, long intervalTicks) {
        this.enabled = enabled;
        this.maxSize = maxSize;
        this.bypassPermission = bypassPermission;
        this.startDelayTicks = startDelayTicks;
        this.intervalTicks = intervalTicks;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public String getBypassPermission() {
        return bypassPermission;
    }

    public long getStartDelayTicks() {
        return startDelayTicks;
    }

    public long getIntervalTicks() {
        return intervalTicks;
    }

    public boolean canBypass(Player player) {
        return !bypassPermission.isEmpty() && player.hasPermission(bypassPermission);
    }

    public static TeleportQueueSettings fromConfiguration(ConfigurationSection section) {
        if (section == null) {
            return disabled();
        }
        boolean enabled = section.getBoolean("enabled", false);
        int maxSize = Math.max(0, section.getInt("max-size", 0));
        String bypassPermission = section.getString("bypass-permission", "");
        if (bypassPermission == null) {
            bypassPermission = "";
        }
        long startDelay = Math.max(0L, section.getLong("start-delay-ticks", 20L));
        long interval = Math.max(0L, section.getLong("interval-ticks", 40L));
        return new TeleportQueueSettings(enabled, maxSize, bypassPermission, startDelay, interval);
    }

    public static TeleportQueueSettings disabled() {
        return new TeleportQueueSettings(false, 0, "", 0L, 0L);
    }
}
