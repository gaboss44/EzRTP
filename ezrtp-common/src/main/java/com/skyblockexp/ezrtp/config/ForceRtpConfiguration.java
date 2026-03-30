package com.skyblockexp.ezrtp.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Configuration for the /forcertp command behavior.
 */
public final class ForceRtpConfiguration {

    private final String defaultWorld;
    private final boolean bypassCooldown;
    private final boolean bypassPermission;
    private final boolean bypassSafety;

    private ForceRtpConfiguration(String defaultWorld, boolean bypassCooldown,
                                  boolean bypassPermission, boolean bypassSafety) {
        this.defaultWorld = defaultWorld;
        this.bypassCooldown = bypassCooldown;
        this.bypassPermission = bypassPermission;
        this.bypassSafety = bypassSafety;
    }

    public String getDefaultWorld() {
        return defaultWorld;
    }

    public boolean shouldBypassCooldown() {
        return bypassCooldown;
    }

    public boolean shouldBypassPermission() {
        return bypassPermission;
    }

    public boolean shouldBypassSafety() {
        return bypassSafety;
    }

    public static ForceRtpConfiguration fromConfiguration(ConfigurationSection section) {
        if (section == null) {
            return defaults();
        }

        String defaultWorld = section.getString("default-world", "world");
        if (defaultWorld == null) {
            defaultWorld = "world";
        }

        ConfigurationSection bypassSection = section.getConfigurationSection("bypass");
        boolean bypassCooldown = bypassSection != null && bypassSection.getBoolean("cooldown", true);
        boolean bypassPermission = bypassSection != null && bypassSection.getBoolean("permission", true);
        boolean bypassSafety = bypassSection != null && bypassSection.getBoolean("safety", false);

        return new ForceRtpConfiguration(defaultWorld, bypassCooldown, bypassPermission, bypassSafety);
    }

    public static ForceRtpConfiguration defaults() {
        return new ForceRtpConfiguration("world", true, true, false);
    }
}