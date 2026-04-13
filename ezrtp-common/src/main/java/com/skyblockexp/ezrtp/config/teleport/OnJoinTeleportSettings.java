package com.skyblockexp.ezrtp.config.teleport;

import org.bukkit.configuration.ConfigurationSection;

public final class OnJoinTeleportSettings {
    private final boolean enabled;
    private final boolean onlyFirstJoin;
    private final String bypassPermission;
    private final long delayTicks;

    public OnJoinTeleportSettings(boolean enabled, boolean onlyFirstJoin, String bypassPermission, long delayTicks) {
        this.enabled = enabled;
        this.onlyFirstJoin = onlyFirstJoin;
        this.bypassPermission = bypassPermission;
        this.delayTicks = delayTicks;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean onlyFirstJoin() {
        return onlyFirstJoin;
    }

    public String bypassPermission() {
        return bypassPermission;
    }

    public long delayTicks() {
        return delayTicks;
    }

    public static OnJoinTeleportSettings fromConfiguration(ConfigurationSection section) {
        if (section == null) {
            return new OnJoinTeleportSettings(false, false, "", 20L);
        }
        boolean enabled = section.getBoolean("enabled", false);
        boolean onlyFirstJoin = section.getBoolean("only-first-join", false);
        String bypassPermission = section.getString("bypass-permission", "");
        long delay = Math.max(0L, section.getLong("delay-ticks", 40L));
        return new OnJoinTeleportSettings(enabled, onlyFirstJoin, bypassPermission == null ? "" : bypassPermission, delay);
    }
}
